import { Component, OnInit, SecurityContext, ViewChild } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { MessageService } from '../services/message.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MessageComponent } from "../message/message.component";
import { RateLimitingService } from '../services/rateLimiting.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
    selector: 'app-authentication',
    standalone: true,
    templateUrl: './authentication.component.html',
    styleUrls: ['./authentication.component.css'],
    imports: [
        FormsModule,
        CommonModule,
        MessageComponent
    ]
})

export class AuthenticationComponent implements OnInit {
  // Declaring properties for user registration form fields
  firstName: string = '';
  lastName: string = '';
  usernameReg: string = '';
  passwordReg: string = '';
  confirmPassword: string = '';

  // Declaring properties for login form fields
  usernameLog: string = '';
  passwordLog: string = '';

  // Property for sanitized content
  sanitizedContent: SafeHtml = '';


  /**
   * Constructor function to inject services.
   * @param authService Service for authentication related operations.
   * @param router Router for navigation and route handling.
   * @param messageService Message service for managing messages displayed to the user.
   */
  constructor(
    private authService: AuthService, 
    private messageService: MessageService,
    private router: Router,
    private rateLimitingService: RateLimitingService,
    private sanitizer: DomSanitizer
  ) {}
    
  ngOnInit() {
    // Logs out the user when the login route is accessed
    this.authService.logout().then(() => {
      console.log('Logging out');
    });
  }

  // Sanitize input
  sanitizeInput(input: string): string {
    return this.sanitizer.sanitize(SecurityContext.HTML, input) || '';
  }
  

  private isPasswordStrong(password: string): boolean {
    const strongPasswordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    return strongPasswordRegex.test(password);
  }

  /**
   * Handles user registration
   */
  async onRegister(): Promise<void> {

    // Sanitize user inputs
    const sanitizedFirstName = this.sanitizeInput(this.firstName);
    const sanitizedLastName = this.sanitizeInput(this.lastName);
    const sanitizedUsername = this.sanitizeInput(this.usernameReg);
    const passwordRegStr = this.passwordReg;  


    // Check if the passwords match and display a message accordingly
    if (passwordRegStr !== this.sanitizeInput(this.confirmPassword)) {
      this.messageService.showMessage('Passwords do not match', false);
      return;
    }

    if (!this.isPasswordStrong(passwordRegStr)) {
      this.messageService.showMessage(
        'Password must be at least 8 characters long, contain an uppercase letter, a lowercase letter, a number, and a special character.',
        false
      );
      return;
    }

    // Create a new user object with the form data
    const newUser = {
      firstName: sanitizedFirstName,
      lastName: sanitizedLastName,
      username: sanitizedUsername,
      password: this.sanitizeInput(passwordRegStr),
    };

    // Send the form data to the backend service to register the user.
    // Show a message according to the registration status
    try {
      const response = await this.authService.registerUser(newUser);
      this.messageService.showMessage(response.message, true);
      this.clearForm();
    } catch (error: any) {
      const errorMessage = error.error?.message || 'Unknown error occurred';
      this.messageService.showMessage(errorMessage, false);
    } 
  }

  /**
   * Resets the form fields
   */
  clearForm(): void {
    this.firstName = '';
    this.lastName = '';
    this.usernameReg = '';
    this.passwordReg = '';
    this.confirmPassword = '';
  }

  /**
   * Checks if all the registration fields are valid
   * @returns True if all fields are filled, false otherwise.
   */
  isRegisterFormValid(): boolean {
    return (
      this.firstName.trim() !== '' &&
      this.lastName.trim() !== '' &&
      this.usernameReg.trim() !== '' &&
      this.passwordReg.trim() !== '' &&
      this.confirmPassword.trim() !== ''
    );
  }

  /**
   * Handles the user login
   */
  async onLogin(): Promise<void> {
    // Sanitize user inputs
    const sanitizedUsername = this.sanitizeInput(this.usernameLog);
    const sanitizedPassword = this.sanitizeInput(this.passwordLog);

    const credentials = { username: sanitizedUsername, password: sanitizedPassword };
    console.log('Attempting login with credentials:', credentials);


  
    if (this.rateLimitingService.loginFailed(credentials.username)) {
      this.messageService.showMessage('Too many login attempts. Please try again later.', false);
      return;
    }

    try {
      const response = await this.authService.loginUser(credentials);
      this.messageService.showMessage(response.message, true);
      if (this.authService.checkLoginStatus()) {
        this.router.navigate(['/forum']);
      } else {
        this.messageService.showMessage("Login failed. Please try again.", false);
      }
    } catch (error: any) {
      const errorMessage = error.error?.message || 'Unknown error occurred';
      this.messageService.showMessage(errorMessage, false);
    }
  }
  

  /**
   * Checks if the fields of the login form are filled.
   * @returns True if the form is valid, false otherwise.
   */
  isLoginFormValid(): boolean {
    return this.usernameLog.trim() !== '' && this.passwordLog.trim() !== '';
  }


}