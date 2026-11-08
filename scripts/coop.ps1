[CmdletBinding()]
param(
    [Parameter(Position = 0, Mandatory = $false)]
    [string]$Mode,
    [Parameter(Position = 1, Mandatory = $false)]
    [string]$Endpoint,
    [Parameter(Position = 2, Mandatory = $false)]
    [string]$Name,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$RemainingArgs
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Root = (Resolve-Path (Join-Path $ScriptDir "..")).Path

if (-not $Mode -or -not $Endpoint -or -not $Name -or ($Mode -ne "host" -and $Mode -ne "join")) {
    [Console]::Error.WriteLine("Usage: coop.ps1 {host|join} ADDRESS:PORT NAME [--coop-new=WORLD|--coop-world=WORLD] [--coop-size=30000]")
    exit 2
}

if ($Name -notmatch '^[A-Za-z0-9_-]{1,24}$') {
    [Console]::Error.WriteLine("Invalid player name (1-24 letters/digits/_/-).")
    exit 2
}

if (-not $env:PW_RUNTIME) {
    $env:PW_RUNTIME = Join-Path $Root "runtime\coop-$Mode"
    if (-not (Test-Path $env:PW_RUNTIME)) {
        $legacy = @(Get-ChildItem (Join-Path $Root "runtime") -Directory -Filter "coop-$Mode-*" -ErrorAction SilentlyContinue)
        $exact = Join-Path $Root "runtime\coop-$Mode-$Name"
        if (Test-Path $exact) { $env:PW_RUNTIME = $exact }
        elseif ($legacy.Count -eq 1) { $env:PW_RUNTIME = $legacy[0].FullName }
        elseif ($legacy.Count -gt 1) { throw "Multiple legacy player folders. Set PW_RUNTIME to your original player folder." }
    }
}

$devArgs = @("run", "--coop-$Mode=$Endpoint", "--coop-name=$Name")
if ($RemainingArgs) {
    $devArgs += $RemainingArgs
}

& (Join-Path $ScriptDir "dev.ps1") @devArgs
exit $LASTEXITCODE
