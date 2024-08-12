# e2e_keypad

## 1. `.env` 파일 목록
- e2e-keypad/env
```angular2html
DB_HOST="localhost"
DB_PORT="6379"
NumToHashMap="NumToHash"
```
- frontend/env
```angular2html
REACT_APP_BACKEND_URL=http://localhost:8080/api
```

## 2. 실행
1) `.env` 파일 생성
2) Kotlin 백엔드 실행
3) `./frontend` 디렉토리에서 `npm install` 후 `npm start`