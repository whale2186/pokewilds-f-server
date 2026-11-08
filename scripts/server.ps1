[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [string]$World,
    [Parameter(Position = 1)]
    [string]$Port,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$RemainingArgs
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Root = (Resolve-Path (Join-Path $ScriptDir "..")).Path

# Build engine first
& (Join-Path $ScriptDir "dev.ps1") build
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$runtime = if ($env:PW_RUNTIME) { $env:PW_RUNTIME } else { Join-Path $Root "runtime" }
if (!(Test-Path $runtime)) { New-Item -ItemType Directory -Path $runtime -Force | Out-Null }

Push-Location $runtime
try {
    $java = "java"
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
        $java = Join-Path $env:JAVA_HOME "bin\java.exe"
    }

    $cp = "$Root\build\pokewilds-engine.jar;$Root\vendor\runtime-deps.jar"
    $serverArgs = New-Object System.Collections.Generic.List[string]
    $serverArgs.Add("-Djava.awt.headless=true")
    $serverArgs.Add("-cp")
    $serverArgs.Add($cp)
    $serverArgs.Add("com.pkmngen.game.DedicatedServer")

    if ($World) { $serverArgs.Add($World) }
    if ($Port) { $serverArgs.Add($Port) }
    if ($RemainingArgs) {
        foreach ($arg in $RemainingArgs) {
            $serverArgs.Add($arg)
        }
    }

    $proc = Start-Process -FilePath $java -ArgumentList $serverArgs -NoNewWindow -Wait -PassThru
    exit $proc.ExitCode
}
finally {
    Pop-Location
}
