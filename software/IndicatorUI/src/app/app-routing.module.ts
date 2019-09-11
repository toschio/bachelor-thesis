import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {StatsOverviewComponent} from './components/stats-overview/stats-overview.component';
import {UserStatsComponent} from './components/user-stats/user-stats.component';
import {AssessmentLeapOverviewComponent} from './components/assessment-leap-overview/assessment-leap-overview.component';
import {UserAssessmentLeapComponent} from './components/user-assessment-leap/user-assessment-leap.component';

const routes: Routes = [
  { path: 'user-statistics/:id', component: UserStatsComponent },
  { path: 'user-statistics', component: StatsOverviewComponent },
  { path: 'assessment-statistics/:id', component: UserAssessmentLeapComponent},
  { path: 'assessment-statistics', component: AssessmentLeapOverviewComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
