import {Component, OnInit} from '@angular/core';
import {AppState} from '../../store';
import {select, Store} from '@ngrx/store';
import {ActivatedRoute} from '@angular/router';
import {map, switchMap} from 'rxjs/operators';
import {selectLeapMotion} from '../../store/leap-motion/leap-motion.selectors';
import {Observable} from 'rxjs';
import {AssessmentLeapMotion} from '../../models/LeapMotionModels';
import {LongSummaryStatistics} from '../../models/MdlModels';

@Component({
  selector: 'app-user-assessment-leap',
  templateUrl: './user-assessment-leap.component.html',
  styleUrls: ['./user-assessment-leap.component.sass']
})
export class UserAssessmentLeapComponent implements OnInit {
  leapMotionStat$: Observable<AssessmentLeapMotion>;
  leapMotionCircleTime$: Observable<LongSummaryStatistics>;
  leapMotionSwipeTime$: Observable<LongSummaryStatistics>;
  leapMotionKeyTapTime$: Observable<LongSummaryStatistics>;
  leapMotionScreenTapTime$: Observable<LongSummaryStatistics>;

  constructor(private store: Store<AppState>, private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.leapMotionStat$ = this.route.paramMap.pipe(
      switchMap((params) => {
        const id = params.get('id');
        return this.store.pipe(select(selectLeapMotion, {userID: id}));
      })
    );

    this.leapMotionCircleTime$ = this.leapMotionStat$.pipe(map((value: AssessmentLeapMotion) => {
      if (!value) {
        return undefined;
      }
      if (value.circleDuration) {
        return value.circleDuration;
      }
    }));
    this.leapMotionSwipeTime$ = this.leapMotionStat$.pipe(map((value: AssessmentLeapMotion) => {
      if (!value) {
        return undefined;
      }
      if (value.swipeDuration) {
        return value.swipeDuration;
      }
    }));
    this.leapMotionKeyTapTime$ = this.leapMotionStat$.pipe(map((value: AssessmentLeapMotion) => {
      if (!value) {
        return undefined;
      }
      if (value.keyTapDuration) {
        return value.keyTapDuration;
      }
    }));
    this.leapMotionScreenTapTime$ = this.leapMotionStat$.pipe(map((value: AssessmentLeapMotion) => {
      if (!value) {
        return undefined;
      }
      if (value.screenTapDuration) {
        return value.screenTapDuration;
      }
    }));
  }

}
