import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, ParamMap} from '@angular/router';
import {map, switchMap} from 'rxjs/operators';
import {HttpRequestService} from '../../services/http-request.service';
import {Observable, of} from 'rxjs';
import {LongSummaryStatistics, Stat} from '../../models/MdlModels';
import * as _ from 'lodash';
import {AppState} from '../../store';
import {select, Store} from '@ngrx/store';
import {selectStat} from '../../store/stats/stats.selectors';

/**
 * visualizes an overview of a specific user's statistics
 */
@Component({
  selector: 'app-user-stats',
  templateUrl: './user-stats.component.html',
  styleUrls: ['./user-stats.component.sass']
})
export class UserStatsComponent implements OnInit {
  public userStat$: Observable<Stat>;
  public userStatperDiscussionForumPosts$: Observable<BarData[]>;
  public userStatAverageReplyTimeOfPostPerDiscussion: BarData[];

  public averageForumPosts$: Observable<LongSummaryStatistics | undefined>;
  public averageReplyTime$: Observable<LongSummaryStatistics | undefined>;
  // public userStatAverageReplyTimeOfPostPerDiscussion: BarData[];

  constructor(private store: Store<AppState>, private route: ActivatedRoute) { }

  ngOnInit() {
    // select user id from active route
    this.userStat$ = this.route.paramMap.pipe(
      switchMap((params) => {
        const id = params.get('id');
        return this.store.pipe(select(selectStat, {userID: id}));
      })
    );
    // todo move into selector ?
    // subscribe to userstats averages per discussion
    this.userStatperDiscussionForumPosts$ = this.userStat$.pipe(map((value: Stat) => {
      return _.map(value.forumPostsPerDiscussion, (val, key) => {
        return {name: 'Discussion ' + key, value: val} as BarData;
      });
    }))

    // todo move into selector
    this.averageForumPosts$ = this.userStat$.pipe(map((value: Stat) => {
      if (value && value.averageForumPostsPerDiscussion) {
        return value.averageForumPostsPerDiscussion;
      }
      return undefined;
    }));

    // // todo move into selector
    // // subscribe to userstats averages per discussion
    // this.userStat$.pipe().subscribe((value: Stat) => {
    //   this.userStatperDiscussionForumPosts$ = _.map(value.forumPostsPerDiscussion, (val, key) => {
    //     return {name: 'Discussion ' + key, value: val} as BarData;
    //   });
    // });



    this.averageReplyTime$ = this.userStat$.pipe(map((value: Stat) => {
        if (value && value.averageReplyTimeOfPost) {
          return value.averageReplyTimeOfPost;
        }
        return undefined;
    }));

    // this.userStat$.pipe().subscribe((value: Stat) => {
    //   this.userStatAverageReplyTimeOfPostPerDiscussion = _.map(value.averageReplyTimeOfPostPerDiscussion,
    //     (val, key) => {
    //       return {name: 'Discussion ' + key, value: val.average} as BarData;
    //     });
    // })
  }

}

export interface BarData {
  name: string;
  value: number;
}
