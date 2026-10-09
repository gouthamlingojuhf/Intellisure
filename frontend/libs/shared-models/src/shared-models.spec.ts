import { ClaimStatus, PolicyStatus, QuoteStatus } from './public-api';

describe('shared-models contracts', () => {
  it('exports lifecycle enums used by the applications', () => {
    expect(QuoteStatus.DRAFT).toBe('DRAFT');
    expect(PolicyStatus.IN_FORCE).toBe('IN_FORCE');
    expect(ClaimStatus.FNOL_RECEIVED).toBe('FNOL_RECEIVED');
  });
});
