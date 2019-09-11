import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { StatsOverviewComponent } from './components/stats-overview/stats-overview.component';
import {BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {
  MatButtonModule,
  MatCardModule,
  MatExpansionModule,
  MatListModule,
  MatMenuModule,
  MatTableModule,
  MatToolbarModule
} from '@angular/material';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {CdkTableModule} from '@angular/cdk/table';
import { UserStatsComponent } from './components/user-stats/user-stats.component';
import {BarChartModule, NumberCardModule} from '@swimlane/ngx-charts';
import {FlexLayoutModule, FlexModule} from '@angular/flex-layout';
import { SummaryStatisticsComponent } from './components/summary-statistics/summary-statistics.component';
import {InjectableRxStompConfig, RxStompService, rxStompServiceFactory} from '@stomp/ng2-stompjs';
import {myRxStompConfig} from './services/my-rx-stomp-config';
import { StoreModule } from '@ngrx/store';
import {reducers} from './store';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import {environment} from '../environments/environment';
import { EffectsModule } from '@ngrx/effects';
import {StatsEffects} from './store/stats/stats.effects';
import { AssessmentLeapOverviewComponent } from './components/assessment-leap-overview/assessment-leap-overview.component';
import { UserAssessmentLeapComponent } from './components/user-assessment-leap/user-assessment-leap.component';
import {LeapMotionEffects} from './store/leap-motion/leap-motion.effects';

@NgModule({
  declarations: [
    AppComponent,
    StatsOverviewComponent,
    UserStatsComponent,
    SummaryStatisticsComponent,
    AssessmentLeapOverviewComponent,
    UserAssessmentLeapComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    AppRoutingModule,
    MatListModule,
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    CdkTableModule,
    MatMenuModule,
    BarChartModule,
    MatCardModule,
    FlexLayoutModule,
    NumberCardModule,
    // ngrx store stuff
    StoreModule.forRoot(reducers),
    StoreDevtoolsModule.instrument({maxAge: 25}),
    !environment.production ? StoreDevtoolsModule.instrument() : [],
    EffectsModule.forRoot([StatsEffects, LeapMotionEffects]),
    MatExpansionModule,
    MatToolbarModule,
    MatButtonModule,
  ],
  providers: [
    // for socket
    {
      provide: InjectableRxStompConfig,
      useValue: myRxStompConfig
    },
    {
      provide: RxStompService,
      useFactory: rxStompServiceFactory,
      deps: [InjectableRxStompConfig]
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
