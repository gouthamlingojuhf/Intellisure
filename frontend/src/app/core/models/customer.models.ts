export interface CustomerResponse {
  customerId: string;
  userId: string;
  businessName: string;
  ownerName: string;
  phone?: string | null;
  businessType?: string | null;
  address?: string | null;
  city?: string | null;
  state?: string | null;
  country?: string | null;
  postalCode?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface UpdateCustomerProfileRequest {
  businessName: string;
  ownerName: string;
  phone?: string | null;
  businessType?: string | null;
  address?: string | null;
  city?: string | null;
  state?: string | null;
  country?: string | null;
  postalCode?: string | null;
}
