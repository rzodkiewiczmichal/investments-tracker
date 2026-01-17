import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ApiError } from '../models';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let apiError: ApiError;

      if (error.error && typeof error.error === 'object' && 'traceId' in error.error) {
        apiError = error.error as ApiError;
      } else {
        apiError = {
          timestamp: new Date().toISOString(),
          status: error.status,
          error: error.statusText || 'Error',
          message: error.message || 'An unexpected error occurred',
          path: req.url,
          traceId: 'unknown'
        };
      }

      console.error('API Error:', apiError);
      return throwError(() => apiError);
    })
  );
};
