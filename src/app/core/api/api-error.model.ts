export interface ApiValidationError {
  status: number;
  errors: Record<string, string>;
}