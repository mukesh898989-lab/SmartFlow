import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';

const routes: Routes = [
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },

  {
    path: 'auth',
    loadChildren: () =>
      import('./modules/auth/auth.module').then(m => m.AuthModule)
  },

  {
    path: 'admin',
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ADMIN'] },
    loadChildren: () =>
      import('./modules/admin/admin.module').then(m => m.AdminModule)
  },

  {
    path: 'receptionist',
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['RECEPTIONIST'] },
    loadChildren: () =>
      import('./modules/receptionist/receptionist.module').then(m => m.ReceptionistModule)
  },

  {
    path: 'doctor',
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['DOCTOR'] },
    loadChildren: () =>
      import('./modules/doctor/doctor.module').then(m => m.DoctorModule)
  },

  {
    path: 'patient',
    loadChildren: () =>
      import('./modules/patient/patient.module').then(m => m.PatientModule)
  },

  { path: '**', redirectTo: '/auth/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
