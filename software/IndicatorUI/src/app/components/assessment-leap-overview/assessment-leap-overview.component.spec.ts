import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AssessmentLeapOverviewComponent } from './assessment-leap-overview.component';

describe('AssessmentLeapOverviewComponent', () => {
  let component: AssessmentLeapOverviewComponent;
  let fixture: ComponentFixture<AssessmentLeapOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AssessmentLeapOverviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AssessmentLeapOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
