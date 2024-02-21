/app
|-- /src
|   |-- /main
|   |   |-- /java
|   |   |   |-- com.example.app
|   |   |   |   |-- /data
|   |   |   |   |   |-- /local
|   |   |   |   |   |   |-- /database: do sử dụng Firebase nên không cần folder này, hoặc có thể dùng cho việc caching các dữ liệu thu được từ database
|   |   |   |   |   |   |   |-- AppDatabase.kt
|   |   |   |   |   |   |
|   |   |   |   |   |   |-- /prefs
|   |   |   |   |   |       |-- AppPreferences.kt: lưu cài đặt/tinh chỉnh của người dùng cũng như các giá trị constants/consistent có thể được sử dụng trong quá trình vận hành của app
|   |   |   |   |   |
|   |   |   |   |   |-- /remote: chứa các class networking, kết nối database và network-utilities
|   |   |   |   |       |-- /api
|   |   |   |   |           |-- ApiService.kt
|   |   |   |   |
|   |   |   |   |-- /model: chứa các data class, chứa các class thực hiện serialization và deserialization, kiểm chứng/xác thực data
|   |   |   |   |   |-- User.kt
|   |   |   |   |
|   |   |   |   |-- /controller
|   |   |   |   |
|   |   |   |   |-- /ui
|   |   |   |       |-- /activity
|   |   |   |       |   |-- MainActivity.kt
|   |   |   |       |   |....
|   |   |   |       |
|   |   |   |       |-- /fragment
|   |   |           |   |-- UserFragment.kt
|   |   |           |   |....
|   |   |
|   |   |-- /res
|   |       |-- /layout
|   |       |-- /values
|   |       |....
|
|-- build.gradle
|-- settings.gradle
