import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PazienteDashboard } from './paziente-dashboard';

describe('PazienteDashboard', () => {
  let component: PazienteDashboard;
  let fixture: ComponentFixture<PazienteDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PazienteDashboard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PazienteDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
