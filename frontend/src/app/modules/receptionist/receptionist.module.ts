import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { ReceptionistLayoutComponent } from './layout/receptionist-layout.component';
import { RegisterPatientComponent } from './register-patient/register-patient.component';
import { GenerateTokenComponent } from './generate-token/generate-token.component';
import { QueueManageComponent } from './queue-manage/queue-manage.component';

const routes: Routes = [
  {
    path: '',
    component: ReceptionistLayoutComponent,
    children: [
      { path: '',               redirectTo: 'queue', pathMatch: 'full' },
      { path: 'register',       component: RegisterPatientComponent },
      { path: 'generate-token', component: GenerateTokenComponent },
      { path: 'queue',          component: QueueManageComponent }
    ]
  }
];

@NgModule({
  declarations: [
    ReceptionistLayoutComponent,
    RegisterPatientComponent,
    GenerateTokenComponent,
    QueueManageComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ]
})
export class ReceptionistModule {}
