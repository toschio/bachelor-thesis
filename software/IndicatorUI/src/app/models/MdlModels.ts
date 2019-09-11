class LongSummaryStatisticsValueHolder {
  count: number;
  sum: number;
  min: number;
  max: number;
  average: number;
}

export class LongSummaryStatistics extends LongSummaryStatisticsValueHolder {

  get min(): number {
    return this.count !== 0 ? this.min : 0;
  }

  get max(): number {
    return this.count !== 0 ? this.max : 0;
  }

}

// tslint:disable-next-line:no-empty-interface
export interface AverageForumPostsPerDiscusison extends LongSummaryStatistics {

}

// tslint:disable-next-line:no-empty-interface
export interface AverageReplyTime extends LongSummaryStatistics {
}

export interface ForumPostsPerDiscussion {
  [discussionID: string]: number;
}

// export interface Depth {
// }
//
export interface AverageReplyTimeOfPostPerDiscussion {
  [discussionID: string]: LongSummaryStatistics;
}

export interface Stat {
  // name : {
  userID: string;
  averageForumPostsPerDiscussion: AverageForumPostsPerDiscusison;
  forumPostsPerDiscussion: ForumPostsPerDiscussion;
  forumPosts: number;
  averageReplyTimeOfPost: AverageReplyTime;
  averageReplyTimeOfPostPerDiscussion: AverageReplyTimeOfPostPerDiscussion;
  // averageDepth: number;
  // depth: Depth;
  // averageReplyTimeOfPostPerDiscussion: AverageReplyTimeOfPostPerDiscussion;
  // averageReplyTime: AverageReplyTime;
  // }
}




