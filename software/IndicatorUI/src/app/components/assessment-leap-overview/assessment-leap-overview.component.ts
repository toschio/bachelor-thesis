import { Component, OnInit } from '@angular/core';
import {Observable} from 'rxjs';
import {AssessmentLeapMotion} from '../../models/LeapMotionModels';
import {Router} from '@angular/router';
import {AppState} from '../../store';
import {select, Store} from '@ngrx/store';
import {selectAllLeapMotion} from '../../store/leap-motion/leap-motion.selectors';

@Component({
  selector: 'app-assessment-leap-overview',
  templateUrl: './assessment-leap-overview.component.html',
  styleUrls: ['./assessment-leap-overview.component.sass']
})
export class AssessmentLeapOverviewComponent implements OnInit {

  allLeapMotionAssessments$: Observable<AssessmentLeapMotion[]>;

  constructor(private router: Router, private store: Store<AppState>) { }

  ngOnInit() {
    this.allLeapMotionAssessments$ = this.store.pipe(select(selectAllLeapMotion));
  }

  selectUserAssessmentLeapMotion(row: AssessmentLeapMotion): void {
    this.router.navigateByUrl('assessment-statistics/' + row.userID);
  }
}
