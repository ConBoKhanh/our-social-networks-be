import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './auth.service'; // Adjust path as needed

@Component({
  selector: 'app-new-user',
  template: `
    <div class="new-user-container">
      <div class="loading-spinner" *ngIf="isLoading">
        <div class="spinner"></div>
        <p>{{ loadingMessage }}</p>
      </div>
      
      <div class="success-message" *ngIf="showSuccess">
        <div class="success-icon">✅</div>
        <h2>Chào mừng bạn!</h2>
        <p>{{ successMessage }}</p>
        <button class="btn-primary" (click)="goToDashboard()">
          Tiếp tục
        </button>
      </div>
      
      <div class="error-message" *ngIf="showError">
        <div class="error-icon">❌</div>
        <h2>Có lỗi xảy ra</h2>
        <p>{{ errorMessage }}</p>
        <button class="btn-secondary" (click)="goToLogin()">
          Quay lại đăng nhập
        </button>
      </div>
    </div>
  `,
  styles: [`
    .new-user-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: #fafafa;
      padding: 20px;
    }
    
    .loading-spinner, .success-message, .error-message {
      background: white;
      border-radius: 8px;
      padding: 40px;
      text-align: center;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      max-width: 400px;
      width: 100%;
    }
    
    .spinner {
      border: 4px solid #f3f3f3;
      border-top: 4px solid #0095f6;
      border-radius: 50%;
      width: 40px;
      height: 40px;
      animation: spin 1s linear infinite;
      margin: 0 auto 20px;
    }
    
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
    
    .success-icon, .error-icon {
      font-size: 48px;
      margin-bottom: 20px;
    }
    
    h2 {
      color: #262626;
      margin-bottom: 16px;
    }
    
    p {
      color: #8e8e8e;
      margin-bottom: 24px;
      line-height: 1.4;
    }
    
    .btn-primary, .btn-secondary {
      padding: 12px 24px;
      border: none;
      border-radius: 4px;
      font-weight: 600;
      cursor: pointer;
      font-size: 14px;
      transition: all 0.2s;
    }
    
    .btn-primary {
      background: #0095f6;
      color: white;
    }
    
    .btn-primary:hover {
      background: #1877f2;
    }
    
    .btn-secondary {
      background: #dbdbdb;
      color: #262626;
    }
    
    .btn-secondary:hover {
      background: #c7c7c7;
    }
  `]
})
export class NewUserComponent implements OnInit {
  isLoading = true;
  showSuccess = false;
  showError = false;
  
  loadingMessage = 'Đang xử lý thông tin đăng nhập...';
  successMessage = '';
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.handleAuthCallback();
  }

  private handleAuthCallback() {
    this.route.queryParams.subscribe(params => {
      const accessToken = params['accessToken'];
      const refreshToken = params['refreshToken'];
      const status = params['status'];
      const message = params['message'];
      const isNewUser = params['isNewUser'];
      const error = params['error'];

      if (error) {
        this.showErrorState(error);
        return;
      }

      if (status === 'success' && accessToken && refreshToken) {
        // Store tokens
        this.authService.setTokens(accessToken, refreshToken);
        
        if (isNewUser === 'true') {
          this.showSuccessState(
            message || 'Tài khoản mới đã được tạo thành công! Chào mừng bạn đến với Our Social Networks.'
          );
        } else {
          this.showSuccessState(
            message || 'Đăng nhập thành công! Chào mừng bạn quay lại.'
          );
        }
      } else {
        this.showErrorState('Thông tin đăng nhập không hợp lệ');
      }
    });
  }

  private showSuccessState(message: string) {
    this.isLoading = false;
    this.showSuccess = true;
    this.successMessage = message;
  }

  private showErrorState(message: string) {
    this.isLoading = false;
    this.showError = true;
    this.errorMessage = message;
  }

  goToDashboard() {
    this.router.navigate(['/dashboard']); // Adjust route as needed
  }

  goToLogin() {
    this.router.navigate(['/login']); // Adjust route as needed
  }
}