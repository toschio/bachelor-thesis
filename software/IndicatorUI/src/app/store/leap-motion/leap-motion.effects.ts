import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {map, switchMap} from 'rxjs/operators';
import {HttpRequestService} from '../../services/http-request.service';
import {addLeap, getLeap, LeapActionsUnion} from './leap-motion.actions';

@Injectable()
export class LeapMotionEffects {

  constructor(private actions$: Actions<LeapActionsUnion>, private backendService: HttpRequestService) {

  }
  /**
   * trigger static http request and load data into store
   */
  @Effect()
  getAll$ = this.actions$.pipe(
    ofType(getLeap.type),
    switchMap(action => this.backendService.getAllLeapMotionAssessment()),
    map(leap => addLeap(leap))
  );
}
