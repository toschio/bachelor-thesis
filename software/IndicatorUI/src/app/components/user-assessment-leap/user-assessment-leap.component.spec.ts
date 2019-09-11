import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserAssessmentLeapComponent } from './user-assessment-leap.component';

describe('UserAssessmentLeapComponent', () => {
  let component: UserAssessmentLeapComponent;
  let fixture: ComponentFixture<UserAssessmentLeapComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UserAssessmentLeapComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserAssessmentLeapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
