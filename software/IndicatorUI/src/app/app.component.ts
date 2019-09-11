import {Component, OnDestroy, OnInit} from '@angular/core';
import {RxStompService} from '@stomp/ng2-stompjs';
import { Message } from '@stomp/stompjs';
import {Stat} from './models/MdlModels';
import { Store } from '@ngrx/store';
import {AppState} from './store';
import {addStat, getStat} from './store/stats/stats.actions';
import {addLeap, getLeap} from './store/leap-motion/leap-motion.actions';
import {AssessmentLeapMotion} from './models/LeapMotionModels';
import {Observable, Subscription} from 'rxjs';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.sass']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'IndicatorUI';
  private subscriptions: Subscription[] = [];

  constructor(private stompService: RxStompService, private store: Store<AppState>) {

  }

  ngOnInit(): void {
    // get all data statically
    this.store.dispatch(getStat());
    // add websocket watch to add further data dynamically
    this.subscriptions.push(this.stompService.watch('/topic/mdl_forum_posts/stat').subscribe((next: Message) => {
      // dispatch a add statistics
      this.store.dispatch(addStat([JSON.parse(next.body) as Stat]));
    }));

    // add websocket watch to add further data dynamically
    this.store.dispatch(getLeap());
    this.subscriptions.push(this.stompService.watch('/topic/leap_motion/stat').subscribe((next: Message) => {
      // dispatch add assessment
      this.store.dispatch(addLeap([JSON.parse(next.body) as AssessmentLeapMotion]));
    }));
  }

  ngOnDestroy(): void {
    this.stompService.deactivate();
    this.subscriptions.forEach((next: Subscription) => next.unsubscribe());
  }
}
