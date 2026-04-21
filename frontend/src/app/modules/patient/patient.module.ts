import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { PatientHomeComponent } from './home/patient-home.component';
import { GetTokenComponent } from './get-token/get-token.component';
import { TrackTokenComponent } from './track/track-token.component';

const routes: Routes = [
  { path: '',         component: PatientHomeComponent },
  { path: 'register', component: GetTokenComponent },
  { path: 'track',    component: TrackTokenComponent }
];

@NgModule({
  declarations: [
    PatientHomeComponent,
    GetTokenComponent,
    TrackTokenComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ]
})
export class PatientModule {}
