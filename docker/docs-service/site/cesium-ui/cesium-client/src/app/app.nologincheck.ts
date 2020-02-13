import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

/*
    This class gets injected if keycloak login is not required and always returns true.
*/
@Injectable()
export class AppNoLoginCheck implements CanActivate {
    constructor(protected router: Router) {                      
    }

    canActivate(): boolean {
        return true;
    }
}