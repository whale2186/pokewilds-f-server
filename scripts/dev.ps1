[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [string]$Command = "help",
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$RemainingArgs
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Root = (Resolve-Path (Join-Path $ScriptDir "..")).Path
$Deps = Join-Path $Root "vendor\runtime-deps.jar"
$MainClass = "com.pkmngen.game.desktop.DesktopLauncher"

function Find-JdkBin {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\javac.exe"))) {
        return (Join-Path $env:JAVA_HOME "bin")
    }
    $javac = Get-Command javac -ErrorAction SilentlyContinue
    if ($javac) {
        return (Split-Path -Parent $javac.Source)
    }
    throw "Java Development Kit (javac) not found. Please set JAVA_HOME or add JDK to PATH."
}

function Build-Engine {
    $jdkBin = Find-JdkBin
    $javac = Join-Path $jdkBin "javac.exe"
    $jar = Join-Path $jdkBin "jar.exe"

    $buildDir = Join-Path $Root "build"
    $logsDir = Join-Path $Root "logs"
    if (!(Test-Path $buildDir)) { New-Item -ItemType Directory -Path $buildDir | Out-Null }
    if (!(Test-Path $logsDir)) { New-Item -ItemType Directory -Path $logsDir | Out-Null }

    $sources = Get-ChildItem -Path (Join-Path $Root "engine\src\main\java") -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
    if (-not $sources -or $sources.Count -eq 0) {
        [Console]::Error.WriteLine("No engine Java sources found.")
        exit 1
    }

    $uuid = [System.Guid]::NewGuid().ToString("N")
    $classesDir = Join-Path $buildDir "classes_$uuid"
    $sourcesFile = Join-Path $buildDir "sources_$uuid.txt"
    New-Item -ItemType Directory -Path $classesDir | Out-Null

    try {
        $quotedSources = $sources | ForEach-Object { "`"$_`"" }
        [System.IO.File]::WriteAllLines($sourcesFile, $quotedSources, [System.Text.Encoding]::UTF8)

        $buildLog = Join-Path $logsDir "build.log"
        $javacArgs = @(
            "--release", "8",
            "-g",
            "-encoding", "UTF-8",
            "-cp", $Deps,
            "-d", $classesDir,
            "@$sourcesFile"
        )

        $proc = Start-Process -FilePath $javac -ArgumentList $javacArgs -NoNewWindow -Wait -PassThru -RedirectStandardOutput $buildLog -RedirectStandardError $buildLog
        if ($proc.ExitCode -ne 0) {
            Get-Content $buildLog | ForEach-Object { [Console]::Error.WriteLine($_) }
            exit $proc.ExitCode
        }

        $tempJar = Join-Path $buildDir "pokewilds-engine-$uuid.jar"
        $jarArgs = @("--create", "--file", $tempJar, "-C", $classesDir, ".")
        $procJar = Start-Process -FilePath $jar -ArgumentList $jarArgs -NoNewWindow -Wait -PassThru
        if ($procJar.ExitCode -ne 0) {
            # Fall back to -cf
            $jarArgsFallback = @("-cf", $tempJar, "-C", $classesDir, ".")
            $procJarFallback = Start-Process -FilePath $jar -ArgumentList $jarArgsFallback -NoNewWindow -Wait -PassThru
            if ($procJarFallback.ExitCode -ne 0) {
                [Console]::Error.WriteLine("Error creating jar artifact.")
                exit $procJarFallback.ExitCode
            }
        }

        $finalJar = Join-Path $buildDir "pokewilds-engine.jar"
        Move-Item -Path $tempJar -Destination $finalJar -Force
        Write-Host "Built $($sources.Count) Java source files: $finalJar"
    }
    finally {
        if (Test-Path $sourcesFile) { Remove-Item -Path $sourcesFile -Force -ErrorAction SilentlyContinue }
        if (Test-Path $classesDir) { Remove-Item -Path $classesDir -Recurse -Force -ErrorAction SilentlyContinue }
    }
}

switch ($Command.ToLower()) {
    "build" {
        Build-Engine
    }
    { $_ -in @("run", "debug") } {
        Build-Engine
        $runtime = if ($env:PW_RUNTIME) { $env:PW_RUNTIME } else { Join-Path $Root "runtime" }
        foreach ($sub in @("cache", "config", "data", "tmp")) {
            $dir = Join-Path $runtime $sub
            if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
        }
        $settingsPath = Join-Path $runtime "settings.txt"
        if (!(Test-Path $settingsPath)) {
            $vendorSettings = Join-Path $Root "vendor\pokewilds-v0.8.11-otherplatforms\settings.txt"
            if (Test-Path $vendorSettings) {
                Copy-Item -Path $vendorSettings -Destination $settingsPath -Force
            }
        }

        Push-Location $runtime
        try {
            $jdkBin = Find-JdkBin
            $java = Join-Path $jdkBin "java.exe"
            $javaArgs = New-Object System.Collections.Generic.List[string]
            $javaArgs.Add("-Djava.io.tmpdir=$(Join-Path $runtime 'tmp')")

            if ($Command.ToLower() -eq "debug") {
                $javaArgs.Add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:5005")
            }

            $cp = "$Root\build\pokewilds-engine.jar;$Deps"
            $javaArgs.Add("-cp")
            $javaArgs.Add($cp)
            $javaArgs.Add($MainClass)

            if ($RemainingArgs) {
                foreach ($arg in $RemainingArgs) {
                    $javaArgs.Add($arg)
                }
            }

            $proc = Start-Process -FilePath $java -ArgumentList $javaArgs -NoNewWindow -Wait -PassThru
            exit $proc.ExitCode
        }
        finally {
            Pop-Location
        }
    }
    "test" {
        $py = Get-Command python -ErrorAction SilentlyContinue
        if (!$py) { $py = Get-Command py -ErrorAction SilentlyContinue }
        if (!$py) { $py = Get-Command python3 -ErrorAction SilentlyContinue }
        if (!$py) {
            [Console]::Error.WriteLine("python not found in PATH.")
            exit 1
        }
        & $py.Source -m unittest discover -s (Join-Path $Root "tests") -v
        exit $LASTEXITCODE
    }
    "rea" {
        $reaCmd = Join-Path $Root "tools\rea-cli\node_modules\.bin\rea.cmd"
        if (Test-Path $reaCmd) {
            & $reaCmd $RemainingArgs
            exit $LASTEXITCODE
        }
        $reaMjs = Join-Path $Root "tools\rea-cli\node_modules\rea-agents\scripts\rea.mjs"
        if (Test-Path $reaMjs) {
            & node $reaMjs $RemainingArgs
            exit $LASTEXITCODE
        }
        [Console]::Error.WriteLine("rea CLI not found in tools/rea-cli.")
        exit 1
    }
    default {
        Write-Host "Usage: dev.ps1 {build|run [dev]|debug [dev]|test|rea ARGS...}"
        exit 1
    }
}
