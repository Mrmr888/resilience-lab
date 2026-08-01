[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$jobs = [System.Collections.Generic.List[System.Management.Automation.Job]]::new()

function Start-LabService {
    param(
        [Parameter(Mandatory)] [string] $Name,
        [Parameter(Mandatory)] [string] $JarPath
    )

    if (-not (Test-Path -LiteralPath $JarPath)) {
        throw "Missing JAR for $Name at $JarPath. Run 'mvn package' first."
    }

    $job = Start-Job -Name $Name -ScriptBlock {
        param($Jar, $WorkingDirectory)
        Set-Location -LiteralPath $WorkingDirectory
        & java -jar $Jar 2>&1
    } -ArgumentList $JarPath, $projectRoot
    $jobs.Add($job)
}

function Wait-ForEndpoint {
    param(
        [Parameter(Mandatory)] [string] $Url,
        [int] $TimeoutSeconds = 75
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            return Invoke-RestMethod -Uri $Url -TimeoutSec 3
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    throw "Timed out waiting for $Url."
}

try {
    Start-LabService "registry" (Join-Path $projectRoot "service-registry/target/service-registry-0.1.0-SNAPSHOT.jar")
    Wait-ForEndpoint "http://localhost:8761/actuator/health" | Out-Null

    Start-LabService "inventory" (Join-Path $projectRoot "inventory-service/target/inventory-service-0.1.0-SNAPSHOT.jar")
    Start-LabService "order" (Join-Path $projectRoot "order-service/target/order-service-0.1.0-SNAPSHOT.jar")
    Start-LabService "gateway" (Join-Path $projectRoot "api-gateway/target/api-gateway-0.1.0-SNAPSHOT.jar")

    Wait-ForEndpoint "http://localhost:8082/actuator/health" | Out-Null
    Wait-ForEndpoint "http://localhost:8081/actuator/health" | Out-Null
    Wait-ForEndpoint "http://localhost:8080/actuator/health" | Out-Null
    Wait-ForEndpoint "http://localhost:8080/api/operations/scenario" -TimeoutSeconds 90 | Out-Null
    Wait-ForEndpoint "http://localhost:8080/api/overview" -TimeoutSeconds 90 | Out-Null

    $normalScenario = @{ mode = "NORMAL"; delayMs = 0 } | ConvertTo-Json
    Invoke-RestMethod -Uri "http://localhost:8080/api/operations/scenario" -Method Put -ContentType "application/json" -Body $normalScenario | Out-Null

    $order = @{ sku = "SKU-SMOKE-01"; quantity = 1 } | ConvertTo-Json
    $success = Invoke-RestMethod -Uri "http://localhost:8080/api/orders/simulate" -Method Post -ContentType "application/json" -Body $order
    if ($success.outcome -ne "SUCCEEDED") {
        throw "Expected SUCCEEDED, received $($success.outcome)."
    }

    $errorScenario = @{ mode = "ERROR"; delayMs = 0 } | ConvertTo-Json
    Invoke-RestMethod -Uri "http://localhost:8080/api/operations/scenario" -Method Put -ContentType "application/json" -Body $errorScenario | Out-Null
    $degraded = Invoke-RestMethod -Uri "http://localhost:8080/api/orders/simulate" -Method Post -ContentType "application/json" -Body $order
    if ($degraded.outcome -ne "DEGRADED") {
        throw "Expected DEGRADED, received $($degraded.outcome)."
    }

    Invoke-RestMethod -Uri "http://localhost:8080/api/operations/scenario" -Method Put -ContentType "application/json" -Body $normalScenario | Out-Null
    $overview = Invoke-RestMethod -Uri "http://localhost:8080/api/overview"

    Write-Host "Smoke test passed: success=$($success.outcome), failure=$($degraded.outcome), events=$($overview.totalRequests)"
} catch {
    foreach ($job in $jobs) {
        Write-Warning "--- $($job.Name) output ---"
        Receive-Job -Job $job -ErrorAction Continue | Select-Object -Last 30 | Out-Host
    }
    throw
} finally {
    foreach ($job in $jobs) {
        Stop-Job -Job $job -ErrorAction SilentlyContinue
        Remove-Job -Job $job -Force -ErrorAction SilentlyContinue
    }
}
