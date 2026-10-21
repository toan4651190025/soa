$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $PSScriptRoot
$runDirectory = Join-Path $root ".run"
$services = @("admin-gateway", "dangky-service", "detai-service", "sinhvien-service")

foreach ($service in $services) {
    $pidFile = Join-Path $runDirectory "$service.pid"
    if (-not (Test-Path $pidFile)) { continue }
    $processId = 0
    if ([int]::TryParse((Get-Content $pidFile -Raw).Trim(), [ref]$processId)) {
        $process = Get-CimInstance Win32_Process -Filter "ProcessId = $processId" -ErrorAction SilentlyContinue
        if ($process -and $process.CommandLine -like "*$service-1.0.0.jar*") {
            Stop-Process -Id $processId -ErrorAction SilentlyContinue
            Write-Host "Stopped $service (PID $processId)"
        } elseif ($process) {
            Write-Warning "PID $processId no longer belongs to $service; left process untouched."
        }
    }
    Remove-Item -LiteralPath $pidFile -Force -ErrorAction SilentlyContinue
}
