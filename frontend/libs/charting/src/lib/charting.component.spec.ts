import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChartingComponent } from './charting.component';

describe('ChartingComponent', () => {
  let component: ChartingComponent;
  let fixture: ComponentFixture<ChartingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChartingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ChartingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
