import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { DoctorLayoutComponent } from './layout/doctor-layout.component';
import { DoctorQueueComponent } from './queue/doctor-queue.component';

const routes: Routes = [
  {
    path: '',
    component: DoctorLayoutComponent,
    children: [
      { path: '',      redirectTo: 'queue', pathMatch: 'full' },
      { path: 'queue', component: DoctorQueueComponent }
    ]
  }
];

@NgModule({
  declarations: [DoctorLayoutComponent, DoctorQueueComponent],
  imports: [CommonModule, RouterModule.forChild(routes)],
  // CommonModule provides NgIf, NgFor, DatePipe, etc.
})
export class DoctorModule {}
