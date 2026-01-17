export interface ValidationError {
  field: string;
  message: string;
  rejectedValue?: unknown;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  details?: ValidationError[];
  path: string;
  traceId: string;
}
