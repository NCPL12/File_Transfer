import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-syngene-audit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './syngene-audit.component.html',
  styleUrl: './syngene-audit.component.css'
})
export class SyngeneAuditComponent {
  fromDate: string = '';
  toDate: string = '';
  errorMessage: string = '';
  private apiBaseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  downloadAuditReport() {
    this.errorMessage = '';

    if (!this.isValidDateRange()) {
      this.errorMessage = 'Please select both From Date and To Date.';
      return;
    }

    if (!this.isToDateAfterFromDate()) {
      this.errorMessage = 'To Date must be after From Date.';
      return;
    }

    const formattedFrom = this.formatDateForSQL(this.fromDate);
    const formattedTo = this.formatDateForSQL(this.toDate);
    const downloadUrl = `${this.apiBaseUrl}/audit-report/download?startDate=${encodeURIComponent(formattedFrom)}&endDate=${encodeURIComponent(formattedTo)}`;
    window.open(downloadUrl, '_blank');
  }

  formatDateForSQL(date: string): string {
    return date.replace('T', ' ') + ':00';
  }

  isValidDateRange(): boolean {
    return this.fromDate !== '' && this.toDate !== '';
  }

  isToDateAfterFromDate(): boolean {
    return new Date(this.toDate) > new Date(this.fromDate);
  }
}
