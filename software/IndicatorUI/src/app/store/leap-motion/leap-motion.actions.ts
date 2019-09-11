
import { createAction, union, props } from '@ngrx/store';
import {AssessmentLeapMotion} from '../../models/LeapMotionModels';

export enum LeapActionTypes {
  GET_LEAP = '[Leap] get leap', // get stats dynamically
  ADD_LEAP = '[Leap] add leap',
  // todo hier alle weiteren action typen
}
export const getLeap = createAction(LeapActionTypes.GET_LEAP, () => ({}));
export const addLeap = createAction(LeapActionTypes.ADD_LEAP, (leap: AssessmentLeapMotion[]) => ({leap}));


const all = union({getLeap, addLeap})

export type LeapActionsUnion = typeof all;
