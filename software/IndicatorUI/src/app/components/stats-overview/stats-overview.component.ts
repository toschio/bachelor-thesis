import { Component, OnInit } from '@angular/core';
import {HttpRequestService} from '../../services/http-request.service';
import {Stat} from '../../models/MdlModels';
import * as _ from 'lodash';
import {Router} from '@angular/router';
import {Observable} from 'rxjs';
import {select, Store} from '@ngrx/store';
import {AppState} from '../../store';
import {selectAllStats} from '../../store/stats/stats.selectors';

/**
 * visualizes an overview of all users' statistics
 */
@Component({
  selector: 'app-stats-overview',
  templateUrl: './stats-overview.component.html',
  styleUrls: ['./stats-overview.component.sass']
})
export class StatsOverviewComponent implements OnInit {

  allStats$: Observable<Stat[]>;

  constructor(private router: Router, private store: Store<AppState>) { }

  ngOnInit() {
    this.allStats$ = this.store.pipe(select(selectAllStats));  // this.statsService.stats$;
  }

  selectUserStat(row: Stat) {
    this.router.navigateByUrl('user-statistics/' + row.userID);
  }
}
