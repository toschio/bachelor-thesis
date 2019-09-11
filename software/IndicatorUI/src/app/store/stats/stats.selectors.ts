import {AppState} from '../index';
import {StatsState} from './stats.reducer';
import {Stat} from '../../models/MdlModels';
import {createSelector} from '@ngrx/store';
import * as _ from 'lodash';
import {BarData} from '../../components/user-stats/user-stats.component';


const getStatsState = (state: AppState): StatsState => state.stats;

const getStats = (state: StatsState): Stat[] => state.stats;

export const selectAllStats = createSelector(getStatsState, getStats);

export const selectStat = createSelector(
  selectAllStats,
  (stats, props) => _.find(stats, (next: Stat) => next.userID === props.userID)
);
