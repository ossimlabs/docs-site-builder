import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppAuthGuard } from './app.authguard';
import { AppNoLoginCheck } from './app.nologincheck';
import { AccessDeniedComponent } from './access-denied/access-denied.component';
import { AppComponent } from './app.component';
import { environment } from '../environments/environment';

const routes: Routes = [
  { path: 'denied', component: AccessDeniedComponent, canActivate: [environment.KEYCLOACK_ACTIVATE? AppAuthGuard: AppNoLoginCheck] },
  { path: ':session', component: AppComponent, data: {  roles: ['kelly-user'] }, canActivate: [environment.KEYCLOACK_ACTIVATE? AppAuthGuard: AppNoLoginCheck] },
  { path: '**', redirectTo: '', data: {  roles: ['kelly-user'] }, canActivate: [environment.KEYCLOACK_ACTIVATE? AppAuthGuard: AppNoLoginCheck] },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
  providers: [AppAuthGuard, AppNoLoginCheck]
})
export class AppRoutingModule { }
