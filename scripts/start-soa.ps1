$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$runDirectory = Join-Path $root ".run"
$logDirectory = Join-Path $runDirectory "logs"
$java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME "bin\java.exe" } else { "java.exe" }
$serviceKey = if ($env:INTERNAL_SERVICE_KEY) { $env:INTERNAL_SERVICE_KEY } else { "soa-demo-internal-key-change-me" }

New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null

$applications = @(
    @{ Name = "sinhvien-service"; Jar = "services\sinhvien-service\target\sinhvien-service-1.0.0.jar"; Url = "http://localhost:8081/api/sinhvien" },
    @{ Name = "detai-service"; Jar = "services\detai-service\target\detai-service-1.0.0.jar"; Url = "http://localhost:8082/api/detai" },
    @{ Name = "dangky-service"; Jar = "services\dangky-service\target\dangky-service-1.0.0.jar"; Url = "http://localhost:8083/api/dangky" },
    @{ Name = "admin-gateway"; Jar = "gateway\target\admin-gateway-1.0.0.jar"; Url = "http://localhost:8080/" }
)

foreach ($application in $applications) {
    $jarPath = Join-Path $root $application.Jar
    if (-not (Test-Path $jarPath)) {
        throw "Build first: missing $jarPath. Run .\mvnw.cmd package."
    }
    $stdout = Join-Path $logDirectory "$($application.Name).out.log"
    $stderr = Join-Path $logDirectory "$($application.Name).err.log"
    $argumentList = "-jar `"$jarPath`""
    $process = Start-Process -FilePath $java -ArgumentList $argumentList -WorkingDirectory $root `
        -RedirectStandardOutput $stdout -RedirectStandardError $stderr -PassThru -WindowStyle Hidden
    Set-Content -Path (Join-Path $runDirectory "$($application.Name).pid") -Value $process.Id
    Write-Host "Started $($application.Name) (PID $($process.Id))"
}

foreach ($application in $applications) {
    $ready = $false
    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        try {
            $headers = if ($application.Name -eq "admin-gateway") { @{} } else { @{ "X-Internal-Service-Key" = $serviceKey } }
            Invoke-RestMethod -Uri $application.Url -Headers $headers -TimeoutSec 2 | Out-Null
            $ready = $true
            break
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    if (-not $ready) {
        & (Join-Path $PSScriptRoot "stop-soa.ps1")
        throw "Service did not become ready: $($application.Name). Check .run\logs."
    }
    Write-Host "Ready $($application.Name) at $($application.Url)"
}
Write-Host "Admin UI: http://localhost:8080 (default local login: admin / admin123)"
