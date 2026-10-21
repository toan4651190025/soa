const endpoints = { sinhvien: "/api/sinhvien", detai: "/api/detai", dangky: "/api/dangky" };
const config = {
  sinhvien: {
    title: "Sinh viên", subtitle: "Danh sách sinh viên thực hiện đồ án tốt nghiệp", list: "Hồ sơ sinh viên", add: "Thêm sinh viên",
    columns: ["MÃ SINH VIÊN", "SINH VIÊN", "NGÀNH ĐÀO TẠO", "THAO TÁC"],
    fields: [
      ["maSinhVien", "Mã sinh viên", "text", true], ["hoTen", "Họ và tên", "text", true],
      ["email", "Email", "email", true], ["nganh", "Ngành", "text", true]
    ]
  },
  detai: {
    title: "Đề tài", subtitle: "Quản lý danh mục đề tài và chỉ tiêu hướng dẫn", list: "Danh mục đề tài", add: "Thêm đề tài",
    columns: ["MÃ ĐỀ TÀI", "TÊN ĐỀ TÀI", "GIẢNG VIÊN HƯỚNG DẪN", "CHỈ TIÊU", "THAO TÁC"],
    fields: [
      ["maDeTai", "Mã đề tài", "text", true], ["tenDeTai", "Tên đề tài", "text", true],
      ["giangVienHuongDan", "Giảng viên hướng dẫn", "text", true], ["chiTieu", "Chỉ tiêu", "number", true, "min=1"],
      ["moTa", "Mô tả", "textarea", false]
    ]
  },
  dangky: {
    title: "Đăng ký & điểm", subtitle: "Theo dõi phân công, tiến độ và kết quả đồ án", list: "Danh sách đăng ký", add: "Thêm đăng ký",
    columns: ["SINH VIÊN", "ĐỀ TÀI", "TRẠNG THÁI", "ĐIỂM HƯỚNG DẪN", "ĐIỂM PHẢN BIỆN", "ĐIỂM HỘI ĐỒNG", "TỔNG KẾT", "THAO TÁC"],
    fields: []
  },
  accounts: {
    title: "Tài khoản sinh viên", subtitle: "Cấp và quản lý quyền đăng nhập cổng sinh viên", list: "Tài khoản đã cấp", add: "Cấp tài khoản",
    columns: ["MÃ SINH VIÊN", "SINH VIÊN", "TRẠNG THÁI TÀI KHOẢN", "NGÀY CẤP", "THAO TÁC"],
    fields: []
  }
};

const state = { active: "sinhvien", rows: [], students: [], topics: [], accounts: [], editId: null, toastTimer: null, csrfToken: null, role: null };
const element = (id) => document.getElementById(id);

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, (char) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[char]);
}

function displayStatus(value) {
  return ({ DANG_KY: "Đăng ký", DANG_THUC_HIEN: "Đang thực hiện", DA_HOAN_THANH: "Đã hoàn thành" })[value] || value || "Đăng ký";
}

async function request(url, options = {}) {
  const method = (options.method || "GET").toUpperCase();
  const headers = { "Content-Type": "application/json", ...options.headers };
  if (!["GET", "HEAD", "OPTIONS"].includes(method) && state.csrfToken) headers["X-CSRF-TOKEN"] = state.csrfToken;
  const response = await fetch(url, { ...options, headers });
  if (!response.ok) {
    let message = `Yêu cầu thất bại (${response.status})`;
    try {
      const body = await response.json();
      message = body.detail || body.message || message;
    } catch { /* Keep the HTTP status message. */ }
    const error = new Error(message);
    error.status = response.status;
    throw error;
  }
  return response.status === 204 ? null : response.json();
}

function showLogin() {
  element("login-screen").hidden = false;
  element("app-shell").hidden = true;
  element("student-portal").hidden = true;
  element("login-username").focus();
}

function showApplication() {
  element("login-screen").hidden = true;
  element("app-shell").hidden = state.role !== "ADMIN";
  element("student-portal").hidden = state.role !== "STUDENT";
  if (state.role === "ADMIN") loadData();
  if (state.role === "STUDENT") loadStudentPortal();
}

async function initializeApp() {
  try {
    const session = await request("/api/auth/session");
    state.csrfToken = session.csrfToken;
    if (!session.authenticated) {
      showLogin();
      return;
    }
    state.role = session.role;
    showApplication();
  } catch (error) {
    showLogin();
    element("login-error").textContent = `Không thể kết nối máy chủ: ${error.message}`;
  }
}

async function login(event) {
  event.preventDefault();
  element("login-error").textContent = "";
  element("login-button").disabled = true;
  const credentials = Object.fromEntries(new FormData(event.currentTarget).entries());
  try {
    if (!state.csrfToken) {
      const session = await request("/api/auth/session");
      state.csrfToken = session.csrfToken;
    }
    const session = await request("/api/auth/login", { method: "POST", body: JSON.stringify(credentials) });
    state.csrfToken = session.csrfToken;
    state.role = session.role;
    showApplication();
  } catch (error) {
    element("login-error").textContent = error.message;
  } finally {
    element("login-button").disabled = false;
  }
}

async function logout() {
  try {
    await request("/api/auth/logout", { method: "POST" });
    state.csrfToken = null;
    state.role = null;
    element("login-password").value = "";
    element("login-error").textContent = "";
    element("student-portal").hidden = true;
    showLogin();
    const session = await request("/api/auth/session");
    state.csrfToken = session.csrfToken;
  } catch (error) {
    showToast(error.message);
  }
}

async function loadData() {
  try {
    const requests = [request(endpoints.sinhvien), request(endpoints.detai), request(endpoints.dangky)];
    if (state.active === "accounts") requests.push(request("/api/admin/accounts"));
    const [students, topics, registrations, accounts = state.accounts] = await Promise.all(requests);
    state.students = students;
    state.topics = topics;
    state.accounts = accounts;
    state.rows = { sinhvien: students, detai: topics, dangky: registrations, accounts }[state.active];
    updateMetrics(students, topics, registrations);
    element("nav-accounts").textContent = state.accounts.length;
    element("add-button").hidden = state.active === "accounts"
      && state.students.every((student) => state.accounts.some((account) => account.studentId === student.id));
    renderTable();
    element("last-updated").textContent = `Cập nhật lúc ${new Date().toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })}`;
  } catch (error) {
    if (error.status === 401) {
      state.csrfToken = null;
      showLogin();
    }
    element("table-body").innerHTML = `<tr><td class="empty-state" colspan="8">${escapeHtml(error.message)} · Kiểm tra dịch vụ rồi tải lại.</td></tr>`;
    showToast(error.message);
  }
}

function updateMetrics(students, topics, registrations) {
  element("metric-students").textContent = students.length;
  element("metric-topics").textContent = topics.length;
  element("metric-registrations").textContent = registrations.length;
  element("nav-students").textContent = students.length;
  element("nav-topics").textContent = topics.length;
  element("nav-registrations").textContent = registrations.length;
  const scored = registrations.filter((item) => item.diemTongKet !== null);
  const average = scored.length ? (scored.reduce((sum, item) => sum + item.diemTongKet, 0) / scored.length).toFixed(2) : null;
  element("metric-score-note").textContent = average ? `Điểm trung bình ${average} / 10` : "Chưa có dữ liệu điểm";
}

function renderTable() {
  const view = config[state.active];
  element("table-head").innerHTML = `<tr>${view.columns.map((label) => `<th>${label}</th>`).join("")}</tr>`;
  const search = element("search-input").value.trim().toLocaleLowerCase("vi");
  const status = element("status-filter").value;
  const rows = state.rows.filter((row) => {
    const matchesText = JSON.stringify(row).toLocaleLowerCase("vi").includes(search);
    return matchesText && (state.active !== "dangky" || !status || row.trangThai === status);
  });
  element("result-count").textContent = `${rows.length} bản ghi`;
  element("status-filter").hidden = state.active !== "dangky";
  if (!rows.length) {
    element("table-body").innerHTML = `<tr><td class="empty-state" colspan="${view.columns.length}">Không có dữ liệu phù hợp.</td></tr>`;
    return;
  }
  element("table-body").innerHTML = rows.map((row) => `<tr>${rowCells(row)}<td>${actionButtons(row.id)}</td></tr>`).join("");
  element("table-body").querySelectorAll("[data-edit]").forEach((button) => button.addEventListener("click", () => openDialog(Number(button.dataset.edit))));
  element("table-body").querySelectorAll("[data-delete]").forEach((button) => button.addEventListener("click", () => deleteRow(Number(button.dataset.delete))));
  element("table-body").querySelectorAll("[data-toggle-account]").forEach((button) => button.addEventListener("click", () => toggleAccount(Number(button.dataset.toggleAccount))));
}

function rowCells(row) {
  if (state.active === "sinhvien") return `<td><span class="code">${escapeHtml(row.maSinhVien)}</span></td><td><span class="cell-primary">${escapeHtml(row.hoTen)}</span><span class="cell-secondary">${escapeHtml(row.email)}</span></td><td>${escapeHtml(row.nganh)}</td>`;
  if (state.active === "detai") return `<td><span class="code">${escapeHtml(row.maDeTai)}</span></td><td><span class="cell-primary">${escapeHtml(row.tenDeTai)}</span><span class="cell-secondary">${escapeHtml(row.moTa || "Chưa có mô tả")}</span></td><td>${escapeHtml(row.giangVienHuongDan)}</td><td>${row.chiTieu}</td>`;
  if (state.active === "accounts") return `<td><span class="code">${escapeHtml(row.maSinhVien)}</span></td><td><span class="cell-primary">${escapeHtml(row.hoTen)}</span></td><td><span class="status-pill ${row.enabled ? "done" : "pending"}">${row.enabled ? "Đang hoạt động" : "Đã khóa"}</span></td><td>${new Date(row.createdAt).toLocaleDateString("vi-VN")}</td>`;
  const statusClass = row.trangThai === "DA_HOAN_THANH" ? "done" : row.trangThai === "DANG_KY" ? "pending" : "";
  const score = (value) => value === null ? `<span class="score-muted">—</span>` : escapeHtml(Number(value).toFixed(1));
  const total = row.diemTongKet === null ? `<span class="score-muted">Chưa chấm</span>` : `<span class="score">${escapeHtml(Number(row.diemTongKet).toFixed(2))}</span><span class="cell-secondary">${row.ketQua === "DAT" ? "ĐẠT" : "CHƯA ĐẠT"}</span>`;
  return `<td><span class="cell-primary">${escapeHtml(row.tenSinhVien)}</span><span class="cell-secondary">${escapeHtml(row.maSinhVien)}</span></td><td><span class="cell-primary">${escapeHtml(row.tenDeTai)}</span></td><td><span class="status-pill ${statusClass}">${escapeHtml(displayStatus(row.trangThai))}</span></td><td>${score(row.diemHuongDan)}</td><td>${score(row.diemPhanBien)}</td><td>${score(row.diemHoiDong)}</td><td>${total}</td>`;
}

function actionButtons(id) {
  if (state.active === "accounts") {
    const account = state.rows.find((row) => row.id === id);
    return `<div class="row-actions"><button class="text-action" type="button" data-toggle-account="${id}">${account.enabled ? "Khóa" : "Mở khóa"}</button><button class="text-action danger" type="button" data-delete="${id}">Xóa</button></div>`;
  }
  return `<div class="row-actions"><button class="text-action" type="button" data-edit="${id}">Sửa</button><button class="text-action danger" type="button" data-delete="${id}">Xóa</button></div>`;
}

function setView(view) {
  state.active = view;
  document.querySelectorAll(".nav-item").forEach((button) => button.classList.toggle("active", button.dataset.view === view));
  const viewConfig = config[view];
  element("page-title").textContent = viewConfig.title;
  element("page-subtitle").textContent = viewConfig.subtitle;
  element("list-title").textContent = viewConfig.list;
  element("add-label").textContent = viewConfig.add;
  element("add-button").hidden = view === "accounts" && state.students.every((student) => state.accounts.some((account) => account.studentId === student.id));
  element("search-input").value = "";
  loadData();
}

function fieldMarkup([name, label, type, required, extra = ""], value = "") {
  const full = type === "textarea" || name === "tenDeTai" ? " full" : "";
  const safeValue = escapeHtml(value);
  let control;
  if (type === "textarea") control = `<textarea id="field-${name}" name="${name}" ${required ? "required" : ""}>${safeValue}</textarea>`;
  else control = `<input id="field-${name}" name="${name}" type="${type}" value="${safeValue}" ${required ? "required" : ""} ${extra}>`;
  return `<div class="field${full}"><label for="field-${name}">${label}${required ? " *" : ""}</label>${control}</div>`;
}

function registrationFields(row = {}) {
  const studentOptions = state.students.map((student) => `<option value="${student.id}" ${row.sinhVienId === student.id ? "selected" : ""}>${escapeHtml(student.maSinhVien)} · ${escapeHtml(student.hoTen)}</option>`).join("");
  const topicOptions = state.topics.map((topic) => `<option value="${topic.id}" ${row.deTaiId === topic.id ? "selected" : ""}>${escapeHtml(topic.maDeTai)} · ${escapeHtml(topic.tenDeTai)}</option>`).join("");
  const statuses = ["DANG_KY", "DANG_THUC_HIEN", "DA_HOAN_THANH"];
  const statusOptions = statuses.map((status) => `<option value="${status}" ${(row.trangThai || "DANG_KY") === status ? "selected" : ""}>${displayStatus(status)}</option>`).join("");
  const select = (name, label, options) => `<div class="field full"><label for="field-${name}">${label} *</label><select id="field-${name}" name="${name}" required>${options}</select></div>`;
  const scores = [["diemHuongDan", "Điểm hướng dẫn"], ["diemPhanBien", "Điểm phản biện"], ["diemHoiDong", "Điểm hội đồng"]];
  return select("sinhVienId", "Sinh viên", `<option value="">Chọn sinh viên</option>${studentOptions}`)
    + select("deTaiId", "Đề tài", `<option value="">Chọn đề tài</option>${topicOptions}`)
    + select("trangThai", "Trạng thái", statusOptions)
    + scores.map(([name, label]) => fieldMarkup([name, label, "number", false, "min=0 max=10 step=0.1"], row[name] ?? "")).join("");
}

function accountFields() {
  const availableStudents = state.students.filter((student) => !state.accounts.some((account) => account.studentId === student.id));
  const options = availableStudents.map((student) => `<option value="${student.id}">${escapeHtml(student.maSinhVien)} · ${escapeHtml(student.hoTen)}</option>`).join("");
  return `<div class="field full"><label for="field-studentId">Sinh viên *</label><select id="field-studentId" name="studentId" required><option value="">Chọn sinh viên</option>${options}</select></div>`
    + fieldMarkup(["password", "Mật khẩu khởi tạo (ít nhất 10 ký tự)", "password", true, "minlength=10 maxlength=128 autocomplete=new-password"]);
}

function openDialog(id = null) {
  state.editId = id;
  const row = id === null ? {} : state.rows.find((item) => item.id === id);
  element("dialog-title").textContent = id === null ? config[state.active].add : "Chỉnh sửa hồ sơ";
  element("form-fields").innerHTML = state.active === "dangky" ? registrationFields(row)
    : state.active === "accounts" ? accountFields()
      : config[state.active].fields.map((field) => fieldMarkup(field, row?.[field[0]] ?? "")).join("");
  element("form-error").textContent = "";
  element("modal-backdrop").hidden = false;
  document.querySelector("#form-fields input, #form-fields select")?.focus();
}

function closeDialog() {
  element("modal-backdrop").hidden = true;
  element("record-form").reset();
}

async function saveRecord(event) {
  event.preventDefault();
  const formData = new FormData(event.currentTarget);
  const payload = Object.fromEntries(formData.entries());
  if (state.active === "accounts") payload.studentId = Number(payload.studentId);
  if (state.active === "detai") payload.chiTieu = Number(payload.chiTieu);
  if (state.active === "dangky") {
    payload.sinhVienId = Number(payload.sinhVienId);
    payload.deTaiId = Number(payload.deTaiId);
    for (const key of ["diemHuongDan", "diemPhanBien", "diemHoiDong"]) payload[key] = payload[key] === "" ? null : Number(payload[key]);
  }
  const endpoint = state.active === "accounts" ? "/api/admin/accounts" : endpoints[state.active];
  const url = state.editId === null ? endpoint : `${endpoint}/${state.editId}`;
  const method = state.editId === null ? "POST" : "PUT";
  element("save-button").disabled = true;
  try {
    await request(url, { method, body: JSON.stringify(payload) });
    closeDialog();
    showToast(state.active === "accounts" ? "Đã cấp tài khoản. Gửi mã sinh viên và mật khẩu khởi tạo cho sinh viên." : state.editId === null ? "Đã tạo hồ sơ." : "Đã cập nhật hồ sơ.");
    await loadData();
  } catch (error) {
    element("form-error").textContent = error.message;
  } finally {
    element("save-button").disabled = false;
  }
}

async function deleteRow(id) {
  const archive = state.active === "sinhvien" || state.active === "detai";
  const confirmation = state.active === "accounts"
    ? "Xóa tài khoản này? Sinh viên sẽ không thể đăng nhập."
    : archive
      ? "Ngừng sử dụng hồ sơ này? Dữ liệu lịch sử đăng ký sẽ được giữ lại."
      : "Xóa bản ghi đăng ký này? Thao tác không thể hoàn tác.";
  if (!window.confirm(confirmation)) return;
  try {
    const endpoint = state.active === "accounts" ? "/api/admin/accounts" : endpoints[state.active];
    await request(`${endpoint}/${id}`, { method: "DELETE" });
    showToast(state.active === "accounts" ? "Đã xóa tài khoản." : archive ? "Đã ngừng sử dụng hồ sơ." : "Đã xóa đăng ký.");
    await loadData();
  } catch (error) { showToast(error.message); }
}

async function toggleAccount(id) {
  const account = state.rows.find((item) => item.id === id);
  try {
    await request(`/api/admin/accounts/${id}`, { method: "PATCH", body: JSON.stringify({ enabled: !account.enabled }) });
    showToast(account.enabled ? "Đã khóa tài khoản." : "Đã mở khóa tài khoản.");
    await loadData();
  } catch (error) { showToast(error.message); }
}

async function loadStudentPortal() {
  try {
    const [profile, topics, registrations] = await Promise.all([
      request("/api/portal/profile"), request("/api/portal/topics"), request("/api/portal/registrations")
    ]);
    element("portal-student-name").textContent = profile.hoTen;
    element("portal-student-code").textContent = profile.maSinhVien;
    renderStudentTopics(topics, registrations);
    renderStudentRegistrations(registrations, topics);
  } catch (error) {
    if (error.status === 401) {
      state.role = null;
      showLogin();
    }
    element("portal-topics").innerHTML = `<p class="portal-error">${escapeHtml(error.message)}</p>`;
  }
}

function renderStudentTopics(topics, registrations) {
  const registeredTopicIds = new Set(registrations.map((item) => item.deTaiId));
  element("portal-topics").innerHTML = topics.length ? topics.map((topic) => {
    const alreadyRegistered = registeredTopicIds.has(topic.id);
    const noSeats = topic.conCho < 1;
    const buttonText = alreadyRegistered ? "Đã đăng ký" : noSeats ? "Đã đủ chỉ tiêu" : "Đăng ký đề tài";
    return `<article class="topic-card"><div class="topic-card-top"><span class="code">${escapeHtml(topic.maDeTai)}</span><span class="seat-count">${topic.conCho} / ${topic.chiTieu} chỗ còn</span></div><h3>${escapeHtml(topic.tenDeTai)}</h3><p class="topic-advisor">GVHD: ${escapeHtml(topic.giangVienHuongDan)}</p><p class="topic-description">${escapeHtml(topic.moTa || "Chưa có mô tả.")}</p><button class="primary-button topic-register" type="button" data-register-topic="${topic.id}" ${alreadyRegistered || noSeats ? "disabled" : ""}>${buttonText}</button></article>`;
  }).join("") : `<p class="portal-loading">Hiện chưa có đề tài.</p>`;
  element("portal-topics").querySelectorAll("[data-register-topic]").forEach((button) =>
    button.addEventListener("click", () => registerForTopic(Number(button.dataset.registerTopic))));
}

function renderStudentRegistrations(registrations, topics) {
  const body = element("portal-registrations");
  if (!registrations.length) {
    body.innerHTML = `<tr><td colspan="5" class="empty-state">Bạn chưa đăng ký đề tài nào.</td></tr>`;
    return;
  }
  body.innerHTML = registrations.map((item) => {
    const topic = topics.find((entry) => entry.id === item.deTaiId);
    return `<tr><td><span class="cell-primary">${escapeHtml(item.tenDeTai)}</span><span class="cell-secondary">${escapeHtml(topic?.maDeTai || item.maDeTai || "")}</span></td><td>${escapeHtml(topic?.giangVienHuongDan || item.giangVienHuongDan || "—")}</td><td><span class="status-pill ${item.trangThai === "DA_HOAN_THANH" ? "done" : "pending"}">${escapeHtml(displayStatus(item.trangThai))}</span></td><td>${item.diemTongKet === null ? "Chưa chấm" : escapeHtml(Number(item.diemTongKet).toFixed(2))}</td><td><button class="text-action danger" type="button" data-cancel-registration="${item.id}">Hủy đăng ký</button></td></tr>`;
  }).join("");
  body.querySelectorAll("[data-cancel-registration]").forEach((button) =>
    button.addEventListener("click", () => cancelRegistration(Number(button.dataset.cancelRegistration))));
}

async function registerForTopic(topicId) {
  try {
    await request("/api/portal/registrations", { method: "POST", body: JSON.stringify({ deTaiId: topicId }) });
    showToast("Đã gửi đăng ký đồ án.");
    await loadStudentPortal();
  } catch (error) { showToast(error.message); }
}

async function cancelRegistration(id) {
  if (!window.confirm("Bạn muốn hủy đăng ký đề tài này?")) return;
  try {
    await request(`/api/portal/registrations/${id}`, { method: "DELETE" });
    showToast("Đã hủy đăng ký.");
    await loadStudentPortal();
  } catch (error) { showToast(error.message); }
}

function closePasswordDialog() {
  element("password-backdrop").hidden = true;
  element("password-form").reset();
  element("password-error").textContent = "";
}

async function changePassword(event) {
  event.preventDefault();
  const payload = Object.fromEntries(new FormData(event.currentTarget).entries());
  element("change-password-button").disabled = true;
  element("password-error").textContent = "";
  try {
    await request("/api/portal/password", { method: "PUT", body: JSON.stringify(payload) });
    closePasswordDialog();
    showToast("Đã đổi mật khẩu. Hãy dùng mật khẩu mới ở lần đăng nhập sau.");
  } catch (error) {
    element("password-error").textContent = error.message;
  } finally {
    element("change-password-button").disabled = false;
  }
}

function showToast(message) {
  const toast = element("toast");
  toast.textContent = message;
  toast.classList.add("visible");
  clearTimeout(state.toastTimer);
  state.toastTimer = setTimeout(() => toast.classList.remove("visible"), 3200);
}

document.querySelectorAll(".nav-item").forEach((button) => button.addEventListener("click", () => setView(button.dataset.view)));
element("add-button").addEventListener("click", () => openDialog());
element("refresh-button").addEventListener("click", loadData);
element("search-input").addEventListener("input", renderTable);
element("status-filter").addEventListener("change", renderTable);
element("record-form").addEventListener("submit", saveRecord);
element("close-dialog").addEventListener("click", closeDialog);
element("cancel-dialog").addEventListener("click", closeDialog);
element("modal-backdrop").addEventListener("click", (event) => { if (event.target === element("modal-backdrop")) closeDialog(); });
document.addEventListener("keydown", (event) => { if (event.key === "Escape" && !element("modal-backdrop").hidden) closeDialog(); });
element("today-label").textContent = new Intl.DateTimeFormat("vi-VN", { weekday: "long", day: "2-digit", month: "long", year: "numeric" }).format(new Date());
element("login-form").addEventListener("submit", login);
element("logout-button").addEventListener("click", logout);
element("portal-logout-button").addEventListener("click", logout);
element("portal-refresh").addEventListener("click", loadStudentPortal);
element("portal-password-button").addEventListener("click", () => { element("password-backdrop").hidden = false; element("current-password").focus(); });
element("close-password").addEventListener("click", closePasswordDialog);
element("cancel-password").addEventListener("click", closePasswordDialog);
element("password-backdrop").addEventListener("click", (event) => { if (event.target === element("password-backdrop")) closePasswordDialog(); });
element("password-form").addEventListener("submit", changePassword);
document.addEventListener("keydown", (event) => { if (event.key === "Escape" && !element("password-backdrop").hidden) closePasswordDialog(); });
initializeApp();