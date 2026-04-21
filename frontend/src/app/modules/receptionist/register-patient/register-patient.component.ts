import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReceptionistService } from '../../../core/services/receptionist.service';
import { Patient } from '../../../core/models';

@Component({
  selector: 'app-register-patient',
  templateUrl: './register-patient.component.html'
})
export class RegisterPatientComponent {

  form: FormGroup;
  loading = false;
  success = '';
  error = '';
  registeredPatient: Patient | null = null;

  constructor(private fb: FormBuilder, private receptionistService: ReceptionistService) {
    this.form = this.fb.group({
      name:   ['', [Validators.required, Validators.minLength(2)]],
      phone:  ['', [Validators.required, Validators.pattern(/^[0-9]{10,15}$/)]],
      email:  ['', Validators.email],
      age:    [null, [Validators.min(0), Validators.max(150)]],
      gender: ['']
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';
    this.success = '';

    const val = this.form.value;
    const payload = {
      name:   val.name,
      phone:  val.phone,
      email:  val.email  || null,
      age:    val.age,
      gender: val.gender || null
    };

    this.receptionistService.registerPatient(payload).subscribe({
      next: patient => {
        this.registeredPatient = patient;
        this.success = `Patient registered. ID: ${patient.id}`;
        this.form.reset();
        this.loading = false;
      },
      error: err => {
        this.error = err.error?.message ?? 'Registration failed.';
        this.loading = false;
      }
    });
  }
}
