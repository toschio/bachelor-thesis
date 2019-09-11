import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {BarData} from '../user-stats/user-stats.component';
import {Observable} from 'rxjs';
import {take} from 'rxjs/operators';
import {async} from 'rxjs/internal/scheduler/async';
import {LongSummaryStatistics} from '../../models/MdlModels';

/**
 * visualizes a LongSummaryStatistic
 */
@Component({
  selector: 'app-summary-statistics',
  templateUrl: './summary-statistics.component.html',
  styleUrls: ['./summary-statistics.component.sass']
})
export class SummaryStatisticsComponent implements OnChanges {

  @Input() statistics: Observable<LongSummaryStatistics | undefined>;

  numberChartsValues: any[];

  constructor() { }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.statistics === undefined) {
      this.numberChartsValues = [];
      return;
    }
    // map observable to data fields
    this.statistics.subscribe(next => {
      this.numberChartsValues = [];
      this.numberChartsValues.push({name: 'average', value: next.average} as BarData);
      this.numberChartsValues.push({name: 'count', value: next.count} as BarData);
      this.numberChartsValues.push({name: 'sum', value: next.sum} as BarData);
      this.numberChartsValues.push({name: 'max', value: next.max} as BarData);
      this.numberChartsValues.push({name: 'min', value: next.min} as BarData);
    });

  }

}
