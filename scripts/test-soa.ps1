$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$internalKey = if ($env:INTERNAL_SERVICE_KEY) { $env:INTERNAL_SERVICE_KEY } else { "soa-demo-internal-key-change-me" }

function Assert-Status([scriptblock]$Action, [int]$ExpectedStatus, [string]$Description) {
    try {
        $null = & $Action
        $actual = 200
    } catch {
        $actual = [int]$_.Exception.Response.StatusCode
        if ($actual -eq 0) { throw }
    }
    if ($actual -ne $ExpectedStatus) {
        throw "$Description expected HTTP $ExpectedStatus but received HTTP $actual."
    }
    Write-Host "PASS $Description ($actual)"
}

try {
    & (Join-Path $PSScriptRoot "start-soa.ps1")
    $base = "http://localhost:8080"
    Assert-Status { Invoke-RestMethod -Uri "$base/api/sinhvien" } 401 "Gateway rejects anonymous API requests"
    Assert-Status {
        Invoke-RestMethod -Uri "http://localhost:8081/api/sinhvien" -Headers @{ "X-Internal-Service-Key" = "invalid" }
    } 401 "Student service rejects unauthenticated internal requests"

    $webSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $session = Invoke-RestMethod -Uri "$base/api/auth/session" -WebSession $webSession
    $headers = @{ "X-CSRF-TOKEN" = $session.csrfToken }
    $login = @{ username = "admin"; password = $(if ($env:APP_ADMIN_PASSWORD) { $env:APP_ADMIN_PASSWORD } else { "admin123" }) } | ConvertTo-Json -Compress
    Invoke-RestMethod -Uri "$base/api/auth/login" -Method Post -WebSession $webSession -Headers $headers `
        -ContentType "application/json" -Body $login | Out-Null
    Assert-Status {
        Invoke-RestMethod -Uri "$base/api/sinhvien" -Method Post -WebSession $webSession `
            -Headers @{ "X-CSRF-TOKEN" = "invalid" } -ContentType "application/json" -Body "{}"
    } 403 "Gateway enforces CSRF tokens for data changes"

    $suffix = [Guid]::NewGuid().ToString("N").Substring(0, 10)
    $studentBody = @{ maSinhVien = "SV$suffix"; hoTen = "Sinh vien SOA test"; email = "$suffix@soa.test"; nganh = "CNTT" } | ConvertTo-Json -Compress
    $student = Invoke-RestMethod -Uri "$base/api/sinhvien" -Method Post -WebSession $webSession -Headers $headers `
        -ContentType "application/json" -Body $studentBody
    $studentUpdate = @{ maSinhVien = $student.maSinhVien; hoTen = "Sinh vien da cap nhat"; email = $student.email; nganh = $student.nganh } | ConvertTo-Json -Compress
    $student = Invoke-RestMethod -Uri "$base/api/sinhvien/$($student.id)" -Method Put -WebSession $webSession -Headers $headers `
        -ContentType "application/json" -Body $studentUpdate
    $topicBody = @{ maDeTai = "DT$suffix"; tenDeTai = "De tai SOA test"; giangVienHuongDan = "Giang vien test"; chiTieu = 1; moTa = "End-to-end" } | ConvertTo-Json -Compress
    $topic = Invoke-RestMethod -Uri "$base/api/detai" -Method Post -WebSession $webSession -Headers $headers `
        -ContentType "application/json" -Body $topicBody
    $topicUpdate = @{ maDeTai = $topic.maDeTai; tenDeTai = "De tai da cap nhat"; giangVienHuongDan = $topic.giangVienHuongDan; chiTieu = 1; moTa = $topic.moTa } | ConvertTo-Json -Compress
    $topic = Invoke-RestMethod -Uri "$base/api/detai/$($topic.id)" -Method Put -WebSession $webSession -Headers $headers `
        -ContentType "application/json" -Body $topicUpdate

    $registrationBody = @{
        sinhVienId = $student.id; deTaiId = $topic.id; trangThai = "DA_HOAN_THANH"
        diemHuongDan = 8; diemPhanBien = 9; diemHoiDong = 7
    } | ConvertTo-Json -Compress
    $registration = Invoke-RestMethod -Uri "$base/api/dangky" -Method Post -WebSession $webSession -Headers $headers `
        -ContentType "application/json" -Body $registrationBody
    if ($registration.diemTongKet -ne 7.9 -or $registration.ketQua -ne "DAT") {
        throw "Registration service did not return the expected weighted score and result."
    }
    Write-Host "PASS Cross-service registration and weighted score"
    $registrationUpdate = @{
        sinhVienId = $student.id; deTaiId = $topic.id; trangThai = "DANG_THUC_HIEN"
        diemHuongDan = 8; diemPhanBien = 9; diemHoiDong = 7
    } | ConvertTo-Json -Compress
    $registration = Invoke-RestMethod -Uri "$base/api/dangky/$($registration.id)" -Method Put -WebSession $webSession -Headers $headers `
        -ContentType "application/json" -Body $registrationUpdate
    if ($registration.trangThai -ne "DANG_THUC_HIEN") { throw "Registration update did not persist the new status." }
    Write-Host "PASS Update operations through gateway"

    Invoke-RestMethod -Uri "$base/api/dangky/$($registration.id)" -Method Delete -WebSession $webSession -Headers $headers | Out-Null
    $initialStudentPassword = "Student-SOA-123!"
    $accountBody = @{ studentId = $student.id; password = $initialStudentPassword } | ConvertTo-Json -Compress
    $account = Invoke-RestMethod -Uri "$base/api/admin/accounts" -Method Post -WebSession $webSession -Headers $headers `
        -ContentType "application/json" -Body $accountBody
    if ($account.maSinhVien -ne $student.maSinhVien -or -not $account.enabled) {
        throw "Gateway did not create an enabled student account."
    }
    Write-Host "PASS Admin provisions a student account"

    $studentSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $studentCsrf = Invoke-RestMethod -Uri "$base/api/auth/session" -WebSession $studentSession
    $studentHeaders = @{ "X-CSRF-TOKEN" = $studentCsrf.csrfToken }
    $studentLogin = @{ username = $student.maSinhVien; password = $initialStudentPassword } | ConvertTo-Json -Compress
    $studentAuth = Invoke-RestMethod -Uri "$base/api/auth/login" -Method Post -WebSession $studentSession -Headers $studentHeaders `
        -ContentType "application/json" -Body $studentLogin
    if ($studentAuth.role -ne "STUDENT") { throw "Student login did not establish the STUDENT role." }
    Assert-Status { Invoke-RestMethod -Uri "$base/api/admin/accounts" -WebSession $studentSession } 403 "Student cannot access admin account management"
    $profile = Invoke-RestMethod -Uri "$base/api/portal/profile" -WebSession $studentSession
    if ($profile.studentId -ne $student.id) { throw "Student portal returned another student's profile." }
    $availableTopics = Invoke-RestMethod -Uri "$base/api/portal/topics" -WebSession $studentSession
    if (-not ($availableTopics | Where-Object { $_.id -eq $topic.id })) { throw "Student portal omitted the available topic." }
    Write-Host "PASS Student login, profile, and topic catalog"

    $portalRegistrationBody = @{ deTaiId = $topic.id } | ConvertTo-Json -Compress
    $portalRegistration = Invoke-RestMethod -Uri "$base/api/portal/registrations" -Method Post -WebSession $studentSession -Headers $studentHeaders `
        -ContentType "application/json" -Body $portalRegistrationBody
    $myRegistrations = Invoke-RestMethod -Uri "$base/api/portal/registrations" -WebSession $studentSession
    if (-not ($myRegistrations | Where-Object { $_.id -eq $portalRegistration.id -and $_.sinhVienId -eq $student.id })) {
        throw "Student portal did not return the student's own registration."
    }
    $secondStudentBody = @{ maSinhVien = "SV2$suffix"; hoTen = "Sinh vien SOA thu hai"; email = "second-$suffix@soa.test"; nganh = "CNTT" } | ConvertTo-Json -Compress
    $secondStudent = Invoke-RestMethod -Uri "$base/api/sinhvien" -Method Post -WebSession $webSession -Headers $headers `
        -ContentType "application/json" -Body $secondStudentBody
    $secondAccountBody = @{ studentId = $secondStudent.id; password = $initialStudentPassword } | ConvertTo-Json -Compress
    $secondAccount = Invoke-RestMethod -Uri "$base/api/admin/accounts" -Method Post -WebSession $webSession -Headers $headers `
        -ContentType "application/json" -Body $secondAccountBody
    $secondSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $secondCsrf = Invoke-RestMethod -Uri "$base/api/auth/session" -WebSession $secondSession
    $secondHeaders = @{ "X-CSRF-TOKEN" = $secondCsrf.csrfToken }
    $secondLogin = @{ username = $secondStudent.maSinhVien; password = $initialStudentPassword } | ConvertTo-Json -Compress
    Invoke-RestMethod -Uri "$base/api/auth/login" -Method Post -WebSession $secondSession -Headers $secondHeaders `
        -ContentType "application/json" -Body $secondLogin | Out-Null
    $secondRegistrations = Invoke-RestMethod -Uri "$base/api/portal/registrations" -WebSession $secondSession
    if ($secondRegistrations) { throw "Student portal exposed another student's registration." }
    Assert-Status {
        Invoke-RestMethod -Uri "$base/api/portal/registrations/$($portalRegistration.id)" -Method Delete -WebSession $secondSession -Headers $secondHeaders
    } 404 "Student cannot cancel another student's registration"
    Invoke-RestMethod -Uri "$base/api/portal/registrations/$($portalRegistration.id)" -Method Delete -WebSession $studentSession -Headers $studentHeaders | Out-Null
    Write-Host "PASS Student registers, sees only own data, and cancels own topic"

    $newStudentPassword = "Student-SOA-456!"
    $changePasswordBody = @{ currentPassword = $initialStudentPassword; newPassword = $newStudentPassword } | ConvertTo-Json -Compress
    Invoke-RestMethod -Uri "$base/api/portal/password" -Method Put -WebSession $studentSession -Headers $studentHeaders `
        -ContentType "application/json" -Body $changePasswordBody | Out-Null
    Invoke-RestMethod -Uri "$base/api/auth/logout" -Method Post -WebSession $studentSession -Headers $studentHeaders | Out-Null

    $renewedStudentSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $renewedCsrf = Invoke-RestMethod -Uri "$base/api/auth/session" -WebSession $renewedStudentSession
    $renewedHeaders = @{ "X-CSRF-TOKEN" = $renewedCsrf.csrfToken }
    $renewedLogin = @{ username = $student.maSinhVien; password = $newStudentPassword } | ConvertTo-Json -Compress
    Invoke-RestMethod -Uri "$base/api/auth/login" -Method Post -WebSession $renewedStudentSession -Headers $renewedHeaders `
        -ContentType "application/json" -Body $renewedLogin | Out-Null
    Write-Host "PASS Student changes password and signs in with the new password"

    Invoke-RestMethod -Uri "$base/api/admin/accounts/$($account.id)" -Method Patch -WebSession $webSession -Headers $headers `
        -ContentType "application/json" -Body (@{ enabled = $false } | ConvertTo-Json -Compress) | Out-Null
    Assert-Status { Invoke-RestMethod -Uri "$base/api/portal/profile" -WebSession $renewedStudentSession } 401 "Locked student account loses portal access"
    Invoke-RestMethod -Uri "$base/api/admin/accounts/$($account.id)" -Method Delete -WebSession $webSession -Headers $headers | Out-Null
    Invoke-RestMethod -Uri "$base/api/admin/accounts/$($secondAccount.id)" -Method Delete -WebSession $webSession -Headers $headers | Out-Null

    $historyRequest = @{
        sinhVienId = $secondStudent.id; deTaiId = $topic.id; trangThai = "DANG_KY"
    } | ConvertTo-Json -Compress
    $history = Invoke-RestMethod -Uri "$base/api/dangky" -Method Post -WebSession $webSession -Headers $headers `
        -ContentType "application/json" -Body $historyRequest
    Invoke-RestMethod -Uri "$base/api/sinhvien/$($secondStudent.id)" -Method Delete -WebSession $webSession -Headers $headers | Out-Null
    Invoke-RestMethod -Uri "$base/api/detai/$($topic.id)" -Method Delete -WebSession $webSession -Headers $headers | Out-Null
    $historyRows = Invoke-RestMethod -Uri "$base/api/dangky" -WebSession $webSession -Headers $headers
    $savedHistory = $historyRows | Where-Object { $_.id -eq $history.id }
    if (-not $savedHistory -or $savedHistory.maSinhVien -ne $secondStudent.maSinhVien -or $savedHistory.tenDeTai -ne $topic.tenDeTai) {
        throw "Registration history did not retain its student and topic snapshots after source records were archived."
    }
    Assert-Status {
        Invoke-RestMethod -Uri "$base/api/detai/$($topic.id)" -WebSession $webSession -Headers $headers
    } 404 "Archived topics are unavailable to new registrations"
    Invoke-RestMethod -Uri "$base/api/dangky/$($history.id)" -Method Delete -WebSession $webSession -Headers $headers | Out-Null

    Invoke-RestMethod -Uri "$base/api/sinhvien/$($student.id)" -Method Delete -WebSession $webSession -Headers $headers | Out-Null
    Write-Host "PASS CRUD, account cleanup, and independent registration history"
} finally {
    & (Join-Path $PSScriptRoot "stop-soa.ps1")
}
