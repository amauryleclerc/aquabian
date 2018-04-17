import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import {RxWebsocketClient} from './service/rx-websocket-client';
import {SensorService} from './sensor/service/sensor.service';

const appRoutes: Routes = [
  // { path: 'stories', component: StoriesComponent },
  // { path: 'story/:id', component: StoryComponent },
  // { path: 'releases', component: ReleasesComponent },
  // { path: 'sprints', component: SprintsComponent },
  // // { path: 'sprint/:id', component: SprintComponent },
  // // { path: 'graph', component: GraphComponent },
  // // { path: 'plush', component: PlushComponent },
  // // { path: 'settings', component: SettingsComponent },
  // // { path: 'jira/project', component: JiraProjectComponent },
  // // { path: 'jira/sprint/:id', component: JiraSprintComponent },
  // // { path: 'jira/story/:id', component: JiraStoryComponent },
  // { path: '', redirectTo: '/sprints', pathMatch: 'full' },
  // { path: '*', redirectTo: '/sprints', pathMatch: 'full' },
];

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BrowserModule,
    HttpClientModule,
    NgbModule.forRoot(),
    RouterModule.forRoot(appRoutes, { useHash: true }),
  ],
  providers: [RxWebsocketClient],
  bootstrap: [AppComponent]
})
export class AppModule { }
