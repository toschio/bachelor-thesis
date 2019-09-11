import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Stat } from '../models/MdlModels';
import { Observable } from 'rxjs';
import {map} from 'rxjs/operators';
import {environment} from '../../environments/environment';
import {AssessmentLeapMotion} from '../models/LeapMotionModels';

@Injectable({
  providedIn: 'root'
})
export class HttpRequestService {

  private forumPostStatUrl = environment.indicatorURL + 'mdl_forum_posts/stat';
  private leapMotionAssessmentURL = environment.indicatorURL + 'leap_motion_assessment/stat';


  constructor(private http: HttpClient) {}

  getAllStats(): Observable<Stat[]> {
    return this.http.get<Stat[]>(this.forumPostStatUrl);
  }

  getAllLeapMotionAssessment(): Observable<AssessmentLeapMotion[]> {
    return this.http.get<AssessmentLeapMotion[]>(this.leapMotionAssessmentURL);
  }
  getStat(userID: string): Observable<Stat> {
    return this.http.get<Stat>(this.forumPostStatUrl + '/' + userID);
  }
}

