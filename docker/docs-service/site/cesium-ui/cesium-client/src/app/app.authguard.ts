import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { KeycloakService, KeycloakAuthGuard } from 'keycloak-angular';
import { UserService } from './core/services/user.service';

@Injectable()
export class AppAuthGuard extends KeycloakAuthGuard implements CanActivate {

    private redirectedToSession = false;
    constructor(protected router: Router,
                protected keycloakAngular: KeycloakService,
                private userService: UserService) {
        super(router, keycloakAngular);
    }

    isAccessAllowed(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
        return new Promise(async (resolve, reject) => {
            if (!this.authenticated) {
                this.keycloakAngular.login();
                return;
            }

           
            const requiredRoles: Array<string> = route.data.roles;

               // If the route doesn't require a role let the user through
            // Else, check the user's role against the list of allowed roles
            if (!requiredRoles || requiredRoles.length === 0) {
                this.userService.setUserId(this.keycloakAngular.getUsername())
                return resolve(true);
            } else {
                if (!this.roles || this.roles.length === 0) {
                    return resolve(false);
                }

                const granted = this.roles.filter(role => requiredRoles.includes(role)).length > 0;
                if (granted) {
                    this.userService.setUserId(this.keycloakAngular.getUsername())
                    let sessionId;
                    if (this.keycloakAngular.getKeycloakInstance().sessionId != undefined)
                        sessionId = this.keycloakAngular.getKeycloakInstance().sessionId
                    else    
                        sessionId = "default";
                        
                    this.userService.setSessionId(sessionId)
                    if(!this.redirectedToSession)
                        {
                            this.router.navigateByUrl( "?session=" + sessionId);
                            this.redirectedToSession = true;
                        }
                       
                    return resolve(granted);
                } else {
                    return this.router.navigateByUrl('/denied');
                }
            }
        });
    }
}