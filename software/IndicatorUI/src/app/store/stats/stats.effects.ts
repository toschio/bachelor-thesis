import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {addStat, getStat, StatActionsUnion} from './stats.actions';
import {map, switchMap} from 'rxjs/operators';
import {HttpRequestService} from '../../services/http-request.service';

@Injectable()
export class StatsEffects {

  constructor(private actions$: Actions<StatActionsUnion>, private backendService: HttpRequestService) {

  }


  /**
   * trigger static http request and load data into store
   */
  @Effect()
  getALl$ = this.actions$.pipe(
    ofType(getStat.type),
    switchMap(action => this.backendService.getAllStats()),
    map(stats => addStat(stats))
  );
}
