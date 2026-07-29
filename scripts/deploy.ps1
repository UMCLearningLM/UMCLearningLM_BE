param(
    [string]$KeyPath = "C:\learninglm-key.pem",
    [string]$EC2Host = "3.35.22.232",
    [string]$User = "ubuntu",
    [string]$RemoteDir = "/opt/learninglm",
    [string]$ServiceName = "learninglm"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$jarLocal = Join-Path $repoRoot "build\libs\learninglm-0.0.1-SNAPSHOT.jar"
$remote = "${User}@${EC2Host}"

Write-Host "==> Building jar"
Push-Location $repoRoot
& "$repoRoot\gradlew.bat" clean build -x test
Pop-Location
if ($LASTEXITCODE -ne 0) { throw "Gradle build failed" }

Write-Host "==> Uploading jar to $remote`:$RemoteDir"
scp -i $KeyPath $jarLocal "${remote}:${RemoteDir}/learninglm.jar"
if ($LASTEXITCODE -ne 0) { throw "scp failed" }

Write-Host "==> Restarting $ServiceName service"
ssh -i $KeyPath $remote "sudo systemctl restart $ServiceName"
if ($LASTEXITCODE -ne 0) { throw "ssh restart failed" }

Write-Host "==> Waiting for app to come back up"
$ready = $false
for ($i = 0; $i -lt 18; $i++) {
    Start-Sleep -Seconds 5
    $code = ssh -i $KeyPath $remote "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/v3/api-docs"
    if ($code -eq "200") { $ready = $true; break }
    Write-Host "    still starting... (http_code=$code)"
}
if (-not $ready) { throw "App did not become ready within 90s. Check: ssh -i $KeyPath $remote 'sudo journalctl -u $ServiceName -n 50'" }
Write-Host "==> App is up (http_code=200)"

Write-Host "==> Done. Swagger UI: http://${EC2Host}:8080/swagger-ui.html"
