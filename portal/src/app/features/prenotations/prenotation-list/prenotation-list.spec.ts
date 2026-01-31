import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrenotationList } from './prenotation-list';

describe('PrenotationList', () => {
  let component: PrenotationList;
  let fixture: ComponentFixture<PrenotationList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrenotationList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PrenotationList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
