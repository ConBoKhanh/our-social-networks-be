# Các table name để thử nếu vẫn lỗi:

## Option 1: Lowercase
```properties
supabase.domains.user.table=user
```

## Option 2: Lowercase plural  
```properties
supabase.domains.user.table=users
```

## Option 3: Uppercase
```properties
supabase.domains.user.table=User
```

## Option 4: Uppercase plural (hiện tại)
```properties
supabase.domains.user.table=Users
```

## Cách debug:
1. Kiểm tra Supabase Dashboard → Table Editor
2. Xem tên chính xác của table
3. Thử GET request trước: `GET /api/users` để xem có lấy được data không
4. Nếu GET work thì table name đúng, vấn đề có thể ở query syntax