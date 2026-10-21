# Nội dung thuyết trình: Spring Boot và kiến trúc SOA trong dự án

> **Chủ đề:** Phân tích Spring Boot qua hệ thống quản lý đồ án tốt nghiệp  
> **Đối tượng:** Bài thuyết trình môn kiến trúc phần mềm / dịch vụ  
> **Thời lượng gợi ý:** 20–25 phút, chưa tính phần demo và hỏi đáp  
> **Lưu ý:** Phiên bản Spring Boot và công nghệ của dự án được ghi theo `pom.xml` và mã nguồn hiện tại. Các khả năng của Spring nói chung được phân biệt với những gì dự án đã triển khai.
> **Cách sử dụng:** Phần “Nội dung slide” là phần đưa lên PowerPoint; phần “Lời thuyết trình” là ghi chú người nói. Không cần đưa toàn bộ lời thoại lên slide.

---

## Slide 1 — Tiêu đề

### SPRING BOOT VÀ KIẾN TRÚC SOA
#### Phân tích hệ thống quản lý đồ án tốt nghiệp

- Framework: Spring Boot 4.1.1
- Ngôn ngữ: Java 21
- Kiến trúc ứng dụng: Gateway + 3 dịch vụ nghiệp vụ
- Giao tiếp giữa ứng dụng: HTTP/REST/JSON

**Lời thuyết trình**

“Trong bài này, em giới thiệu Spring Boot trước, sau đó phân tích cách framework được sử dụng trong hệ thống quản lý đồ án tốt nghiệp. Em sẽ chỉ rõ đâu là tính năng của Spring Boot, đâu là phần dự án tự xây dựng và các giới hạn hiện tại của kiến trúc.”

---

## Slide 2 — Bài toán và mục tiêu hệ thống

### Bài toán

Quản lý các hoạt động liên quan đến đồ án tốt nghiệp:

- Quản lý hồ sơ sinh viên.
- Quản lý đề tài, giảng viên hướng dẫn và chỉ tiêu.
- Quản lý đăng ký đề tài, trạng thái thực hiện và điểm.
- Cung cấp giao diện riêng cho quản trị viên và sinh viên.

### Mục tiêu thiết kế

- Tách các nhóm nghiệp vụ thành service riêng.
- Giao tiếp qua API thay vì truy cập trực tiếp cơ sở dữ liệu của nhau.
- Giữ lịch sử đăng ký ngay cả khi sinh viên hoặc đề tài được lưu trữ.
- Có thể chạy từng ứng dụng riêng hoặc khởi chạy toàn hệ thống để demo.

**Lời thuyết trình**

“Hệ thống giải quyết một bài toán quản lý tương đối quen thuộc, nhưng được tổ chức thành các ứng dụng riêng. Nhờ vậy, ta có thể nhìn thấy ranh giới dịch vụ và luồng gọi API thay vì gom toàn bộ chức năng vào một ứng dụng nguyên khối.”

---

## Slide 3 — Spring Boot là gì?

### Giới thiệu chung

- **Spring Boot** là nền tảng phát triển ứng dụng Java dựa trên hệ sinh thái Spring.
- Trọng tâm là Java; Spring Boot cũng hỗ trợ phát triển ứng dụng bằng Kotlin và có thể làm việc với các thư viện/hệ sinh thái JVM khác.
- Mục tiêu: giúp tạo ứng dụng độc lập, có thể chạy và triển khai với cấu hình khởi đầu tối thiểu.
- Dự án hiện tại khai báo **Spring Boot 4.1.1** trong Maven parent.
- Dự án dùng **Java 21**.
- Mã nguồn Spring Boot được phát triển công khai trong tổ chức `spring-projects` trên GitHub; hệ sinh thái có tài liệu, dự án con và cộng đồng đóng góp.
- Spring Boot được phân phối theo giấy phép **Apache License 2.0**.

### Cách hiểu đúng

- Spring Boot không thay thế Spring Framework; Boot giúp cấu hình và kết nối các phần của Spring thuận tiện hơn.
- Phiên bản 4.1.1 ở đây là phiên bản **dự án đang khai báo**, không phải tuyên bố phiên bản mới nhất ở mọi thời điểm.
- Đây là phần mềm mã nguồn mở do **Spring team** phát triển công khai trong tổ chức `spring-projects`; mã nguồn Spring Boot dùng giấy phép Apache 2.0.
- Cộng đồng có thể tiếp cận tài liệu chính thức, mã nguồn, issue/discussion trên GitHub và các kênh cộng đồng Spring. Không nên đưa một con số thành viên cố định vì số liệu thay đổi theo thời gian.

**Lời thuyết trình**

“Spring Boot được xây dựng trên Spring Framework và tập trung giảm cấu hình thủ công. Trong dự án này, thông tin có thể kiểm chứng trực tiếp là Spring Boot 4.1.1 và Java 21, được khai báo trong file cấu hình Maven ở thư mục gốc.”

**Chuyển ý:** “Sau khi biết Spring Boot là gì, tiếp theo ta cần phân biệt Boot với Spring Framework và các dự án Spring khác.”

---

## Slide 4 — Spring Boot đứng ở đâu trong hệ sinh thái Spring?

### Các lớp công nghệ có thể kết hợp

| Lớp | Vai trò | Ví dụ trong dự án |
|---|---|---|
| Spring Boot | Khởi tạo ứng dụng, cấu hình tự động, đóng gói/chạy ứng dụng | Maven parent, cấu hình `application.properties` |
| Spring Framework | IoC/DI, Spring MVC, transaction và các nền tảng cốt lõi | Controller, `@Service`, `@Transactional` |
| Spring Data JPA | Tạo lớp truy cập dữ liệu trên JPA | Repository và Hibernate |
| Spring Boot Actuator | Endpoint quản trị/giám sát ứng dụng | Health check trong profile triển khai |
| Flyway | Quản lý phiên bản schema theo migration | Migration cho cấu hình production |
| Spring Security / mã bảo mật dự án | Xác thực và bảo vệ request | Session, CSRF, interceptor và khóa nội bộ |

### Điểm cần nhấn mạnh

Spring Boot là nền tảng tích hợp và cấu hình ứng dụng. JPA, Actuator, Flyway và các thư viện khác là các thành phần được khai báo/kết hợp, không nên gọi tất cả là “một chức năng lõi duy nhất của Boot”.

### Ba khái niệm không nên nhầm

- **Spring Framework:** nền tảng cốt lõi như IoC/DI, AOP, Spring MVC và transaction.
- **Spring Boot:** quy ước, auto-configuration, starter và công cụ để tạo ứng dụng Spring dễ chạy hơn.
- **Spring Cloud / Spring Web Services:** các dự án bổ sung cho một số bài toán phân tán, Gateway, SOAP/XML; không tự xuất hiện chỉ vì dùng Spring Boot.

**Lời thuyết trình**

“Một ứng dụng Spring Boot thường là tổ hợp nhiều dự án trong hệ sinh thái. Boot lo việc khởi động và cấu hình; Spring MVC xử lý web; Spring Data JPA hỗ trợ dữ liệu; còn Actuator và Flyway là những thư viện được bổ sung theo nhu cầu.”

**Chuyển ý:** “Ta đã phân biệt các lớp công nghệ. Tiếp theo là những cơ chế giúp Spring Boot giảm công việc cấu hình.”

---

## Slide 5 — Các đặc điểm nổi bật của Spring Boot

### 1. Auto-configuration

- Dựa trên dependency và cấu hình để tự thiết lập nhiều bean phổ biến.
- Có thể thay thế hoặc tùy chỉnh bằng cấu hình riêng khi yêu cầu dự án khác mặc định.

### 2. Starter dependencies

- Gom các dependency thường dùng thành nhóm theo mục đích.
- Dự án dùng starter cho Web MVC, Data JPA, Actuator và kiểm thử.

### 3. Ứng dụng độc lập

- Có thể đóng gói ứng dụng web thành JAR chạy bằng Java.
- Mỗi service trong dự án có JAR, cổng và cấu hình chạy riêng.

### 4. Cấu hình linh hoạt

- Dùng `application.properties` và biến môi trường.
- Dự án có cấu hình local/demo và profile `production`.

### 5. Hỗ trợ vận hành

- Actuator có thể cung cấp health, metrics và các endpoint quản trị tùy cấu hình.
- Dự án chỉ mở health endpoint cần cho kiểm tra trạng thái.

### Một số khả năng quan trọng khác

- **Externalized configuration:** cùng một ứng dụng có thể nhận cấu hình khác nhau theo môi trường mà không phải sửa mã.
- **Embedded server:** ứng dụng web có thể chạy như tiến trình Java độc lập thay vì bắt buộc cài và triển khai lên application server riêng.
- **Testing support:** hỗ trợ test theo lớp, kiểm thử web và tích hợp; dự án hiện có kiểm thử bảo mật Gateway và script kiểm thử E2E.
- **Không tự động giải quyết mọi yêu cầu:** cần chủ động cấu hình logging, bảo mật, resilience, quan sát và chiến lược dữ liệu.

**Lời thuyết trình**

“Các đặc điểm này giúp nhóm tập trung nhiều hơn vào nghiệp vụ thay vì tự cấu hình mọi thành phần từ đầu. Tuy nhiên, auto-configuration không có nghĩa là ứng dụng tự an toàn hoặc tự sẵn sàng cho production; nhóm vẫn phải cấu hình bảo mật, database và vận hành.”

---

## Slide 6 — Cấu trúc tiêu biểu của một ứng dụng Spring Boot

### Luồng khởi động và xử lý request

```text
SpringApplication.run(...)
          |
          v
Environment + application.properties + biến môi trường
          |
          v
ApplicationContext / IoC Container
          |
    Auto-configuration
          |
Embedded Servlet Container (ứng dụng Web MVC)
          |
HTTP -> DispatcherServlet -> Controller -> Service -> Repository
                                                   |
                                                Database
```

### Vai trò các thành phần nền tảng

- `SpringApplication`: khởi động ứng dụng và tạo application context.
- `Environment`: hợp nhất cấu hình từ file, profile, biến môi trường và tham số.
- **IoC Container:** quản lý vòng đời bean và Dependency Injection.
- **Auto-configuration:** đưa ra cấu hình mặc định có điều kiện; có thể thay thế bằng cấu hình của ứng dụng.
- **Embedded server:** phục vụ ứng dụng web đóng gói thành tiến trình chạy độc lập.
- **Spring MVC:** ánh xạ request HTTP tới controller rồi trả response.

> Đây là sơ đồ khái niệm để thuyết trình; chi tiết bean và server phụ thuộc starter/cấu hình được chọn.

### Ý nghĩa

- **Application class:** điểm vào của ứng dụng và vùng quét component.
- **Controller:** nhận request HTTP, kiểm tra đầu vào và trả response.
- **Service:** xử lý quy tắc nghiệp vụ.
- **Repository:** truy cập dữ liệu.
- **Dependency Injection:** Spring tạo và kết nối các đối tượng cần thiết.

### Trong dự án

- Các service nghiệp vụ có lớp `@SpringBootApplication`, Controller, Service, Repository và Entity.
- Gateway có Controller, client gọi backend, quản lý session và giao diện web tĩnh.
- Lớp khởi động nằm ở package gốc của từng ứng dụng để component scan tìm thấy các thành phần cùng package con.
- Controller tiếp nhận HTTP; Service thực thi nghiệp vụ; Repository làm việc với database; `RestClient` gọi service ngoài qua HTTP.

**Lời thuyết trình**

“SpringApplication khởi động application context; container quản lý dependency; sau đó Spring MVC tiếp nhận request và gọi các tầng nghiệp vụ. Đây là mô hình khái niệm, không phải mọi ứng dụng Spring Boot đều bắt buộc phải có cùng số tầng.”

---

## Slide 7 — Spring Boot và các chuẩn dịch vụ: REST, SOAP, WSDL, UDDI

| Công nghệ/chuẩn | Spring có thể sử dụng? | Tình trạng trong dự án |
|---|---|---|
| HTTP | Có, thông qua Spring MVC/WebFlux và thư viện HTTP | **Có** |
| REST | Có thể xây dựng REST API bằng Spring MVC/WebFlux | **Có** |
| JSON | Có thể trao đổi JSON qua HTTP và bộ chuyển đổi dữ liệu | **Có** |
| SOAP | Có thể tích hợp bằng dự án **Spring Web Services** | **Chưa triển khai** |
| WSDL | Có thể xây dựng/tiêu thụ SOAP contract với Spring-WS và công cụ liên quan | **Chưa có WSDL** |
| UDDI | Không phải tính năng đăng ký dịch vụ mặc định của Spring Boot | **Chưa triển khai** |
| Service discovery | Có thể tích hợp hệ sinh thái Spring Cloud hoặc registry phù hợp | **Không dùng trong demo** |

### Cách diễn đạt chính xác

- REST là **phong cách kiến trúc**; HTTP là giao thức truyền tải; JSON là định dạng dữ liệu.
- REST API thường mô hình hóa tài nguyên bằng URI và dùng các HTTP method như GET, POST, PUT, DELETE; bản thân REST không yêu cầu phải có một file mô tả tương đương WSDL.
- WSDL là ngôn ngữ mô tả hợp đồng dịch vụ web SOAP; SOAP là giao thức trao đổi thông điệp XML.
- UDDI là chuẩn/registry để công bố và khám phá dịch vụ; đây không phải một API được Spring Boot tự sinh.
- Spring Boot không tự biến API thành SOAP/WSDL/UDDI chỉ vì dự án dùng Spring.
- Dự án hiện triển khai API HTTP/REST/JSON; không tuyên bố hỗ trợ SOAP, WSDL hoặc UDDI.

**Lời thuyết trình**

“Spring Boot có thể mở rộng để làm SOAP, nhưng thường cần Spring Web Services và hợp đồng XML/WSDL. UDDI cũng không tự có sẵn trong Boot. Trong sản phẩm của em, giao tiếp đang dùng là REST/JSON, vì vậy em phân biệt rõ khả năng của nền tảng với phần thực sự đã triển khai.”

---

## Slide 8 — SOA: Những nguyên tắc dùng để đánh giá dự án

### Các nguyên tắc quan trọng

1. **Ranh giới dịch vụ theo nghiệp vụ:** service chịu trách nhiệm một miền chức năng rõ ràng.
2. **Hợp đồng dịch vụ tường minh:** API, request/response và lỗi được xác định để bên gọi không phụ thuộc cách cài đặt bên trong.
3. **Loose coupling:** các service trao đổi qua giao diện/hợp đồng, không truy cập trực tiếp database hoặc gọi thẳng class nội bộ của nhau.
4. **Quyền sở hữu dữ liệu:** mỗi service là nơi chịu trách nhiệm chính cho dữ liệu thuộc miền của mình.
5. **Khả năng kết hợp:** một luồng nghiệp vụ có thể sử dụng nhiều service; đổi lại cần quản lý timeout, lỗi và nhất quán dữ liệu.

### Đối chiếu với dự án

| Nguyên tắc | Trạng thái trong dự án |
|---|---|
| Tách theo miền Sinh viên / Đề tài / Đăng ký | Có |
| Hợp đồng HTTP/JSON riêng | Có |
| Database và cấu hình theo service | Có ở mức logic; trong Compose cùng PostgreSQL server |
| Không gọi vòng giữa các service | Đã bỏ lời gọi ngược từ Sinh viên/Đề tài tới Đăng ký |
| Hoàn toàn không phụ thuộc service khác | Chưa đạt: Đăng ký xác minh Sinh viên/Đề tài khi ghi |

**Lời thuyết trình**

“SOA không chỉ là chạy nhiều tiến trình. Cần có ranh giới nghiệp vụ, hợp đồng API, quyền sở hữu dữ liệu và cách xử lý khi dịch vụ khác lỗi. Dự án đã có nhiều yếu tố nền tảng, nhưng cần trình bày đúng mức độ phụ thuộc còn lại.”

**Chuyển ý:** “Dựa trên các nguyên tắc này, ta có thể đọc sơ đồ kiến trúc thực tế của dự án.”

---

## Slide 9 — Kiến trúc tổng thể của dự án

```text
                 Trình duyệt
                     |
           HTTP session + CSRF
                     v
          Admin Gateway :8080
          /        |         \
   REST/JSON   REST/JSON   REST/JSON
      /            |            \
Sinh viên       Đề tài        Đăng ký
  :8081          :8082          :8083
    |               |              |
 DB riêng        DB riêng       DB riêng
                                  |
                           REST xác minh
                           /            \
                    Sinh viên          Đề tài
```

### Bốn ứng dụng Spring Boot

1. **Admin Gateway:** giao diện, xác thực phiên, CSRF, phân quyền và chuyển tiếp API.
2. **Sinh viên Service:** quản lý hồ sơ sinh viên.
3. **Đề tài Service:** quản lý đề tài, giảng viên và chỉ tiêu.
4. **Đăng ký Service:** quản lý đăng ký, trạng thái và kết quả điểm.

### Gateway trong dự án là gì?

- Gateway hiện tại là ứng dụng Spring Boot tự xây dựng, gồm controller, `RestClient`, xác thực và logic proxy.
- Đây **không phải** Spring Cloud Gateway; dự án không khai báo starter Spring Cloud Gateway.
- Spring Cloud Gateway là một lựa chọn riêng nếu sau này cần route/filter, rate limit, discovery hoặc circuit breaker theo cấu hình.

**Lời thuyết trình**

“Gateway là cửa vào của giao diện, còn ba ứng dụng phía sau đại diện cho các nhóm nghiệp vụ. Mỗi ứng dụng sở hữu database riêng về mặt cấu hình. Đăng ký gọi Sinh viên và Đề tài để kiểm tra dữ liệu khi phát sinh đăng ký.”

**Chuyển ý:** “Sơ đồ cho biết thành phần nào tồn tại; tiếp theo là cách một yêu cầu thực sự đi qua các thành phần.”

---

## Slide 10 — Ranh giới dữ liệu và luồng gọi dịch vụ

### Mỗi service chịu trách nhiệm dữ liệu của mình

- Sinh viên quản lý hồ sơ và trạng thái hoạt động của sinh viên.
- Đề tài quản lý thông tin đề tài, chỉ tiêu và trạng thái hoạt động.
- Đăng ký lưu trạng thái đăng ký, điểm và **snapshot** mã/tên sinh viên, đề tài, giảng viên.
- Gateway có database riêng cho tài khoản sinh viên của cổng.

### Luồng tạo đăng ký

1. Người dùng gửi yêu cầu tới Gateway.
2. Gateway xác thực session, quyền và CSRF rồi gọi Đăng ký Service.
3. Đăng ký Service gọi Sinh viên và Đề tài qua REST để xác minh ID và lấy thông tin.
4. Service kiểm tra trùng đăng ký/chỉ tiêu, lưu đăng ký cùng snapshot.
5. Khi đọc lịch sử, dữ liệu snapshot cho phép hiển thị thông tin ngay cả khi hồ sơ/đề tài đã được lưu trữ.

### Giới hạn độc lập

- Không còn phụ thuộc gọi ngược từ Sinh viên/Đề tài sang Đăng ký.
- Nhưng thao tác tạo/cập nhật đăng ký vẫn phụ thuộc Sinh viên và Đề tài đang sẵn sàng.
- Snapshot giữ lịch sử, nhưng **không thay thế** việc xác minh thông tin mới.

### Ví dụ hợp đồng API đang dùng

- Sinh viên: `GET /api/sinhvien/{id}` để tra cứu hồ sơ.
- Đề tài: `GET /api/detai/{id}` để lấy đề tài và chỉ tiêu.
- Đăng ký: `POST /api/dangky` để tạo đăng ký; các API CRUD còn lại nằm dưới `/api/dangky`.
- Gateway gọi backend bằng `RestClient`, gửi `X-Internal-Service-Key`; timeout kết nối 3 giây, đọc 10 giây.
- Endpoint backend dùng nội bộ; người dùng cuối gọi qua Gateway.

### Cách xử lý lỗi đã có

- Không tìm thấy sinh viên/đề tài trả lỗi không tìm thấy.
- Backend cần thiết không phản hồi thì thao tác đăng ký trả lỗi khả dụng dịch vụ (`503`).
- Đọc lịch sử đăng ký dùng snapshot, không cần gọi lại hai service nguồn.

**Lời thuyết trình**

“Đây là điểm thể hiện ranh giới dữ liệu: mỗi service sở hữu dữ liệu của chính nó và trao đổi qua API. Snapshot giúp giữ lịch sử, nhưng không loại bỏ dependency khi tạo đăng ký. Do đó em gọi đây là các service được tách riêng, không phải độc lập tuyệt đối.”

**Chuyển ý:** “Sau khi hiểu luồng tích hợp, ta xem các chức năng mà người dùng nhìn thấy.”

---

## Slide 11 — Tính năng nghiệp vụ được triển khai

### Quản trị viên

- Đăng nhập bằng session.
- Quản lý sinh viên và đề tài.
- Xem, chỉnh sửa và quản lý đăng ký.
- Cấp tài khoản sinh viên, khóa/mở khóa hoặc xóa tài khoản.
- Các request thay đổi dữ liệu được bảo vệ bằng CSRF.

### Sinh viên

- Đăng nhập bằng mã sinh viên và mật khẩu được cấp.
- Xem hồ sơ, danh sách đề tài và đăng ký của bản thân.
- Đăng ký hoặc hủy đăng ký đề tài.
- Đổi mật khẩu.
- Tài khoản bị khóa sẽ không còn truy cập được cổng sinh viên.

### Quy tắc đăng ký và chấm điểm

- Không cho đăng ký trùng một cặp sinh viên/đề tài.
- Kiểm tra chỉ tiêu trước khi tạo đăng ký.
- Điểm tổng kết = `0,3 × hướng dẫn + 0,3 × phản biện + 0,4 × hội đồng`.
- Điểm hợp lệ trong khoảng 0–10; kết quả đạt từ 5,00.

**Lời thuyết trình**

“Các chức năng không nằm trong một controller lớn duy nhất. Giao diện gọi Gateway; Gateway xử lý xác thực và chuyển nghiệp vụ tới service sở hữu dữ liệu tương ứng. Quy tắc đăng ký và tính điểm được xử lý ở Đăng ký Service.”

---

## Slide 12 — Bảo mật và kiểm soát truy cập

- Session-based login cho quản trị viên và sinh viên.
- Gateway phân biệt vai trò ADMIN và STUDENT.
- Request thay đổi dữ liệu yêu cầu CSRF token.
- Backend yêu cầu header nội bộ `X-Internal-Service-Key`.
- Mật khẩu tài khoản sinh viên được lưu dạng băm có salt theo cấu hình dự án.
- Các service không được publish trực tiếp ra host trong Compose; Gateway là cổng vào.
- Cấu hình production có cookie bảo mật, security headers và secret từ môi trường.

### Phạm vi cần nói đúng

- Đây là các biện pháp hiện được cấu hình trong dự án; không nên diễn giải thành chứng nhận bảo mật hoặc bảo đảm tuyệt đối.
- Demo local dùng thông tin đăng nhập mẫu; không dùng tài khoản/mật khẩu mẫu khi triển khai công khai.

**Lời thuyết trình**

“Bảo mật được phân thành nhiều lớp: xác thực phiên ở Gateway, CSRF cho thao tác thay đổi dữ liệu và khóa nội bộ cho backend. Khi thuyết trình, em sẽ nói đây là các biện pháp hiện có trong phạm vi dự án, không khẳng định hệ thống đã thay thế được đánh giá bảo mật độc lập.”

---

## Slide 13 — Cách triển khai dịch vụ

### Local/demo

- Java 21 và Maven Wrapper.
- Mỗi ứng dụng đóng gói thành một JAR riêng.
- H2 dùng cho local/demo; các cổng lần lượt là 8080–8083.
- Script `scripts/start-soa.ps1` khởi động bốn ứng dụng và chờ kiểm tra sẵn sàng.
- Cấu hình H2 riêng cho từng ứng dụng nằm trong thư mục `data\`; dữ liệu local không tự di chuyển sang PostgreSQL.

### Production baseline bằng Docker Compose

- Bốn ứng dụng chạy trong các container riêng.
- PostgreSQL chạy trong một container; mỗi ứng dụng có database và user riêng.
- Flyway quản lý schema; Hibernate dùng `validate` trong profile production.
- Backend chỉ expose trong mạng Compose; Gateway bind localhost để đặt sau reverse proxy HTTPS.
- Có health check, container non-root, filesystem read-only và giới hạn quyền Linux.

### Mức độ triển khai hiện có

- Có cấu hình và tài liệu cho một máy chủ.
- Cần hạ tầng TLS, backup định kỳ, giám sát, khôi phục thử và kiểm thử tải trước khi đưa lên môi trường thật.
- Một instance Đăng ký tuần tự hóa ghi; nếu chạy nhiều replica cần xử lý khóa/đồng thời ở database hoặc cơ chế phân tán.

### Các cách chạy phổ biến trong Spring Boot

1. **Chạy JAR độc lập:** cách dự án sử dụng khi demo.
2. **Container hóa:** đóng gói từng ứng dụng thành image và điều phối bằng Docker Compose hoặc nền tảng khác.
3. **Triển khai lên nền tảng quản lý:** có thể dùng Kubernetes hoặc dịch vụ cloud khi quy mô/yêu cầu phù hợp; dự án hiện không cấu hình Kubernetes.

> Maven multi-module ở đây giúp quản lý mã nguồn/build chung; nó không biến bốn ứng dụng thành một tiến trình duy nhất. Mỗi module Spring Boot vẫn tạo artifact thực thi riêng.

**Lời thuyết trình**

“Maven gom các module để quản lý build, nhưng mỗi ứng dụng vẫn tạo JAR riêng. Local chạy H2 cho thuận tiện; Compose chuyển sang PostgreSQL và migration. Đây là baseline triển khai, còn việc vận hành production phải bổ sung quy trình backup, giám sát và TLS.”

---

## Slide 14 — Ưu điểm của Spring Boot

- **Tăng tốc phát triển:** auto-configuration và starter giảm lượng cấu hình ban đầu.
- **Hệ sinh thái lớn:** tích hợp web, dữ liệu, bảo mật, kiểm thử và vận hành.
- **Phù hợp ứng dụng doanh nghiệp:** hỗ trợ transaction, validation, JPA và nhiều cách tích hợp.
- **Đóng gói linh hoạt:** có thể chạy ứng dụng độc lập bằng JAR/container.
- **Dễ tổ chức API:** Spring MVC giúp xây REST API, Spring Data JPA giảm phần truy vấn lặp.
- **Cộng đồng và tài liệu:** có hệ sinh thái Spring và nguồn mã mở công khai.

### Liên hệ dự án

Spring Boot giúp cùng một phong cách cấu hình/triển khai cho Gateway và ba service, trong khi từng ứng dụng vẫn có module, cổng, API và dữ liệu riêng.

**Lời thuyết trình**

“Ưu điểm lớn nhất của Spring Boot là hệ sinh thái trưởng thành và khả năng tích hợp. Đối với dự án này, nhóm dùng được cùng nền tảng cho web, REST, dữ liệu, kiểm tra health và đóng gói ứng dụng.”

---

## Slide 15 — Hạn chế và so sánh với lựa chọn khác

| Tiêu chí | Spring Boot | Quarkus / Micronaut |
|---|---|---|
| Điểm mạnh thường thấy | Hệ sinh thái Spring rộng, nhiều tích hợp và nhân lực có kinh nghiệm | Tập trung mạnh vào thời gian khởi động, footprint và triển khai cloud/native |
| Khởi động / bộ nhớ | Có thể cao hơn ứng dụng tối giản tùy dependency, cấu hình và cách chạy | Có thể tối ưu tốt cho startup/memory, nhưng kết quả phụ thuộc workload và cấu hình |
| Độ trưởng thành hệ sinh thái | Rộng, nhiều dự án con và tài liệu | Đang phát triển nhanh, lựa chọn tích hợp có thể khác nhau |
| Phù hợp khi | Cần hệ sinh thái Java doanh nghiệp, tích hợp rộng, đội ngũ quen Spring | Ưu tiên cold start/footprint hoặc native image và chấp nhận chọn hệ sinh thái tương ứng |

### Hạn chế cần cân nhắc với Spring Boot

- Nhiều dependency và cấu hình hơn một ứng dụng Java tối giản.
- Auto-configuration tiện lợi nhưng cần hiểu để tránh cấu hình không mong muốn.
- Ứng dụng truyền thống có thể tốn bộ nhớ/khởi động hơn phương án tối ưu native, tùy workload.
- Không tự giải quyết các vấn đề kiến trúc phân tán: retry, circuit breaker, discovery, consistency, observability.
- Chạy nhiều service làm tăng chi phí vận hành: nhiều tiến trình, cấu hình, log, health check và database cần theo dõi.
- Việc cùng dùng Spring Boot không tự làm các service thành SOA; chất lượng phụ thuộc vào ranh giới và hợp đồng do nhóm thiết kế.

### So sánh cần diễn giải thận trọng

- **Spring Framework thuần:** cung cấp nền tảng cốt lõi; Spring Boot bổ sung quy ước, auto-configuration và cách đóng gói/chạy ứng dụng tiện hơn. Đây là quan hệ nền tảng–bổ trợ, không phải hai framework hoàn toàn loại trừ nhau.
- **Jakarta EE:** một nền tảng đặc tả doanh nghiệp với các API/chuẩn riêng; thường phù hợp tổ chức đã dựa vào tiêu chuẩn và application server Jakarta EE.
- **Quarkus/Micronaut:** lựa chọn Java thường được cân nhắc cho startup/footprint hoặc native image; cần đánh giá mức trưởng thành của tích hợp và khả năng đội ngũ.

> So sánh này mang tính định hướng, không phải benchmark. Kết quả thực tế cần đo trên cùng chức năng, cấu hình và hạ tầng.

**Lời thuyết trình**

“Không có framework tốt nhất cho mọi trường hợp. Spring Boot có lợi thế về độ rộng hệ sinh thái; Quarkus và Micronaut thường được cân nhắc khi startup hoặc footprint rất quan trọng. Cần benchmark trên workload thực tế thay vì kết luận chỉ dựa trên tên framework.”

---

## Slide 16 — Lĩnh vực sử dụng và case study của dự án

### Các loại hệ thống phù hợp

- **Thương mại điện tử:** API sản phẩm, giỏ hàng, đơn hàng và tích hợp thanh toán; cần thiết kế rõ giao dịch và idempotency.
- **Tài chính/ngân hàng:** backend nghiệp vụ và tích hợp; đòi hỏi kiểm soát bảo mật, audit, độ sẵn sàng và tuân thủ theo môi trường cụ thể.
- **Logistics:** quản lý đơn, trạng thái vận chuyển và kết nối các hệ thống đối tác.
- **Giáo dục:** hồ sơ, học phần, đăng ký và quản lý kết quả — tương đồng miền nghiệp vụ với case study của dự án.
- **Nền tảng web/mobile:** REST API và backend dùng chung cho nhiều client.
- Các ví dụ trên là **nhóm bài toán phù hợp**, không phải khẳng định một tổ chức cụ thể đang dùng bản dự án này.

### Case study: quản lý đồ án tốt nghiệp

- Gateway gom điểm truy cập và xác thực.
- Service Sinh viên, Đề tài, Đăng ký sở hữu các nhóm dữ liệu/nghiệp vụ riêng.
- REST/JSON nối các ứng dụng.
- Snapshot giúp lưu lịch sử đăng ký khi hồ sơ nguồn được lưu trữ.
- Có thể mở rộng từng ứng dụng theo tải riêng, nhưng cần giải quyết thêm tính sẵn sàng và đồng thời nếu triển khai nhiều replica.

### Kết luận case study

Đây là ví dụ thực hành về cách dùng Spring Boot xây dựng các API và chia ứng dụng theo miền nghiệp vụ; mức độ độc lập vận hành hiện ở mức cơ bản, chưa phải kiến trúc microservice phân tán hoàn chỉnh.

### Kịch bản trình bày trực quan

1. Admin tạo sinh viên và đề tài.
2. Admin cấp tài khoản; sinh viên đăng nhập qua Gateway.
3. Sinh viên xem đề tài và gửi yêu cầu đăng ký.
4. Gateway gọi Đăng ký Service; Đăng ký xác minh hai mã tham chiếu qua API.
5. Đăng ký lưu dữ liệu và snapshot.
6. Admin nhập điểm; hệ thống tính điểm tổng kết theo trọng số.
7. Lưu trữ hồ sơ/đề tài; lịch sử đăng ký vẫn hiển thị từ snapshot.

### Bản đồ endpoint minh họa

| Nhóm | Endpoint tiêu biểu | Ý nghĩa |
|---|---|---|
| Đăng nhập | `POST /api/auth/login` | Xác thực qua Gateway |
| Cổng sinh viên | `GET /api/portal/topics` | Lấy danh sách đề tài |
| Đăng ký | `POST /api/portal/registrations` | Sinh viên gửi yêu cầu đăng ký qua Gateway |
| Hồ sơ | `/api/sinhvien`, `/api/sinhvien/{id}` | API nội bộ của service Sinh viên |
| Đề tài | `/api/detai`, `/api/detai/{id}` | API nội bộ của service Đề tài |
| Nghiệp vụ đăng ký | `/api/dangky`, `/api/dangky/{id}` | API nội bộ của service Đăng ký |

### Chỉ số/điều kiện kiểm chứng

- Test E2E đã bao phủ xác thực, CSRF, quyền truy cập, CRUD, đăng ký/hủy và snapshot lịch sử.
- Bài test xác nhận luồng chức năng trên môi trường local; không đại diện cho kiểm thử tải, HA hoặc kiểm thử PostgreSQL/Docker trên production.

**Lời thuyết trình**

“Case study chính là hệ thống của nhóm. Nó minh họa cách Spring Boot hỗ trợ tạo nhiều ứng dụng REST độc lập về triển khai. Đồng thời, nó cũng cho thấy giới hạn thực tế: service Đăng ký cần các service tham chiếu để xác minh nghiệp vụ.”

**Chuyển ý:** “Case study cho thấy lựa chọn kiến trúc giải quyết được bài toán nào và những nội dung nào cần phát triển tiếp.”

---

## Slide 17 — Đánh giá kiến trúc và hướng phát triển

### Những gì đã đạt

- Bốn ứng dụng Spring Boot riêng, có thể build/chạy dưới artifact riêng.
- Ba service nghiệp vụ chia theo miền và sở hữu dữ liệu theo cấu hình.
- API REST/JSON và Gateway tạo điểm truy cập thống nhất cho UI.
- Đã bỏ vòng gọi giữa Sinh viên/Đề tài và Đăng ký.
- Snapshot bảo toàn nội dung lịch sử; có timeout và phản hồi lỗi khi gọi service không thành công.

### Những gì chưa nên tuyên bố

- Chưa độc lập hoàn toàn vì luồng ghi đăng ký phụ thuộc hai service khác.
- Chưa có service discovery, circuit breaker, message broker hay distributed tracing.
- PostgreSQL được tách database/user nhưng đặt chung một PostgreSQL server trong Compose.
- Chưa có bằng chứng kiểm thử tải/HA/khôi phục trên môi trường production thực tế.

### Hướng cải tiến theo thứ tự ưu tiên

1. Thêm kiểm thử hợp đồng API giữa các service.
2. Thêm retry có giới hạn/circuit breaker phù hợp; tránh retry thao tác ghi thiếu idempotency.
3. Đưa khóa chỉ tiêu/trùng đăng ký vào transaction/constraint phù hợp cho nhiều instance.
4. Bổ sung log tập trung, metrics, tracing và cảnh báo.
5. Kiểm thử migration, backup/restore và tải bằng PostgreSQL thật.
6. Chỉ thêm message broker hoặc service discovery khi nhu cầu thực sự yêu cầu.

**Lời thuyết trình**

“Đánh giá kiến trúc cần chỉ ra cả phần đạt và phần chưa đạt. Dự án có nền tảng tốt để minh họa SOA, nhưng trước khi mở rộng quy mô cần thêm kiểm thử hợp đồng, cơ chế chịu lỗi và quy trình vận hành.”

---

## Slide 18 — Tổng kết

### Điều cần nhớ

1. Spring Boot 4.1.1 + Java 21 là cấu hình khai báo trong dự án.
2. Spring Boot hỗ trợ xây ứng dụng Java độc lập, cấu hình tự động và kết hợp các dự án Spring.
3. Dự án có bốn ứng dụng: một Gateway và ba service nghiệp vụ.
4. Giao tiếp đang triển khai là HTTP/REST/JSON; không có SOAP, WSDL hay UDDI trong mã nguồn hiện tại.
5. Dịch vụ được tách theo nghiệp vụ và dữ liệu, nhưng chưa độc lập tuyệt đối khi có service phụ thuộc bị lỗi.
6. Spring Boot là nền tảng; chất lượng SOA phụ thuộc vào ranh giới nghiệp vụ, hợp đồng API, dữ liệu và vận hành do nhóm thiết kế.

### Câu kết

“Spring Boot giúp triển khai các thành phần kỹ thuật; còn một kiến trúc SOA tốt cần nhóm thiết kế đúng ranh giới dịch vụ, kiểm soát phụ thuộc và bảo đảm vận hành phù hợp.”

---

## Slide 19 — Tài liệu tham khảo

1. Spring Boot — trang dự án: <https://spring.io/projects/spring-boot>
2. Spring Boot Reference Documentation: <https://docs.spring.io/spring-boot/reference/>
3. Auto-configuration: <https://docs.spring.io/spring-boot/reference/using/auto-configuration.html>
4. Cấu trúc mã nguồn Spring Boot: <https://docs.spring.io/spring-boot/reference/using/structuring-your-code.html>
5. Spring MVC / Web: <https://docs.spring.io/spring-boot/reference/web/servlet.html>
6. Spring Boot Actuator: <https://docs.spring.io/spring-boot/reference/actuator/index.html>
7. Spring Framework: <https://spring.io/projects/spring-framework>
8. Spring Data JPA: <https://spring.io/projects/spring-data-jpa>
9. Spring Web Services (SOAP/XML/WS-Security): <https://spring.io/projects/spring-ws>
10. Spring Cloud Gateway (tham khảo; **không phải Gateway đang dùng trong dự án**): <https://spring.io/projects/spring-cloud-gateway>
11. Spring Cloud: <https://spring.io/projects/spring-cloud>
12. Mã nguồn dự án Spring Boot: <https://github.com/spring-projects/spring-boot>
13. Mã nguồn dự án: `pom.xml`, `HELP.md`, `docker-compose.yml`, các module `gateway/` và `services/`.
14. Spring Community: <https://spring.io/community>
15. Spring Cloud — các mẫu hệ thống phân tán: <https://spring.io/projects/spring-cloud>

> Thời điểm đối chiếu nguồn trực tuyến: 21/10/2026. Khi nộp bài, nên kiểm tra lại phiên bản framework và trang hỗ trợ vì thông tin phát hành thay đổi theo thời gian.

---

## Gợi ý trình diễn sau phần slide

1. Mở trang đăng nhập tại `http://localhost:8080`.
2. Đăng nhập local bằng tài khoản demo hiển thị trên trang.
3. Thể hiện giao diện quản trị và thao tác quản lý sinh viên/đề tài/đăng ký.
4. Giải thích luồng: Browser → Gateway → service nghiệp vụ → database riêng.
5. Nếu trình bày chức năng đăng ký, nêu rõ Đăng ký gọi Sinh viên/Đề tài để xác minh tham chiếu.
6. Kết thúc bằng giới hạn: REST/JSON đã có; SOAP/WSDL/UDDI chưa triển khai.

---

## Câu hỏi phản biện có thể gặp

### 1. “Đây là SOA hay microservices?”

**Trả lời gợi ý:** “Dự án tổ chức theo hướng SOA, chia ứng dụng theo miền nghiệp vụ và giao tiếp REST. Các service được build/chạy riêng nhưng còn phụ thuộc đồng bộ khi tạo đăng ký, nên em không khẳng định đây là kiến trúc microservices hoàn chỉnh hay độc lập tuyệt đối.”

### 2. “Vì sao không dùng SOAP/WSDL?”

**Trả lời gợi ý:** “Phạm vi hiện tại là ứng dụng web nội bộ, nên nhóm chọn REST/JSON để giao tiếp đơn giản. Spring Boot có thể tích hợp SOAP qua Spring Web Services, nhưng dự án chưa có yêu cầu tương tác SOAP nên chưa triển khai.”

### 3. “Mỗi service có database riêng, vậy dữ liệu có bị trùng?”

**Trả lời gợi ý:** “Mỗi service giữ dữ liệu có thẩm quyền trong miền của mình. Đăng ký lưu snapshot tên/mã tại thời điểm đăng ký để bảo toàn lịch sử, không coi snapshot đó là bản sao có thẩm quyền thay cho dữ liệu hiện tại của Sinh viên hay Đề tài.”

### 4. “Nếu Sinh viên hoặc Đề tài ngừng hoạt động thì sao?”

**Trả lời gợi ý:** “Đọc lịch sử đăng ký vẫn dùng snapshot. Nhưng tạo/cập nhật đăng ký cần xác minh tham chiếu nên có thể trả lỗi dịch vụ không khả dụng. Đây là dependency đồng bộ hiện có và là điểm cần cải tiến nếu yêu cầu tính sẵn sàng cao.”

### 5. “Có thể scale từng service không?”

**Trả lời gợi ý:** “Có thể triển khai nhiều instance về mặt ứng dụng, nhưng cần xử lý khóa chỉ tiêu và giao dịch ghi để tránh vượt quota khi có nhiều instance Đăng ký; đồng thời cần load balancing và giám sát phù hợp.”

### 6. “Gateway này có phải Spring Cloud Gateway không?”

**Trả lời gợi ý:** “Không. Đây là Gateway tự xây bằng Spring Boot, dùng controller và RestClient. Spring Cloud Gateway là dự án riêng trong hệ Spring Cloud, có thể được cân nhắc khi cần hệ thống routing/filter chuyên dụng.”

---

## Gợi ý thiết kế PowerPoint

- **Tỉ lệ:** 16:9; dùng một màu chủ đạo xanh đậm, màu nhấn xanh lá hoặc cam.
- **Font:** tiêu đề sans-serif đậm; thân bài dễ đọc; giữ cỡ chữ đủ lớn khi chiếu.
- **Mỗi slide:** một thông điệp chính, tối đa 4–6 ý ngắn; phần chi tiết nói bằng lời, không đưa nguyên văn speaker notes lên slide.
- **Sơ đồ:** vẽ Gateway và ba service thành các khối; dùng mũi tên liền cho HTTP/REST, database nằm dưới service sở hữu nó.
- **Nhất quán thuật ngữ:** gọi “Gateway” và “service”; tránh dùng lẫn “module”, “service” và “database” như cùng một khái niệm.
- **Màu trạng thái:** xanh = đã triển khai; vàng = có giới hạn; xám = chưa triển khai.
- **Hình minh họa phù hợp:** sơ đồ kiến trúc, luồng sequence đăng ký, pipeline build/run; tránh dùng hình logo không rõ nguồn hoặc số liệu cộng đồng không có dẫn chứng.
