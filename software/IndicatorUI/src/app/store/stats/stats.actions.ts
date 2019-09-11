
import { createAction, union, props } from '@ngrx/store';
import {Stat} from '../../models/MdlModels';

export enum StatsActionTypes {
  GET_STAT = '[Stat] get stats', // get stats dynamically
  ADD_STAT = '[Stat] add stats',
  // todo hier alle weiteren action typen
}
export const getStat = createAction(StatsActionTypes.GET_STAT, () => ({}));
export const addStat = createAction(StatsActionTypes.ADD_STAT, (stat: Stat[]) => ({stat}));


const all = union({getStat, addStat})

export type StatActionsUnion = typeof all;
