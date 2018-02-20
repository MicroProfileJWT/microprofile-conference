import {Injectable} from '@angular/core';
import {Http} from '@angular/http';
import {RequestOptions} from '@angular/http';
import {RequestMethod} from '@angular/http';
import {Headers} from '@angular/http';
import {Router} from '@angular/router';
import { AuthService } from './auth.service';

@Injectable()
export class JwtService {

    EXPIRES_IN = 'expires_in';
    EXPIRES_AT = 'expires_at';
    SERVICE = 'requested_service';
    REQUESTED_SERVICE_COOKIE = 'liber8-requested-service';

    STATE_LENGTH = 20;
    DEFAULT_EXP_SEC = 7200;
    REQUESTED_SERVICE_COOKIE_EXP_SEC = 300;

    constructor(private http: Http, private router: Router) { }

    getWithJwt(requestUrl: string): Promise<any> {
        return this.requestWithJwt(requestUrl, "GET");
    }
    requestWithJwt(requestUrl: string, requestMethod: string | RequestMethod, bodyData?: Object): Promise<any> {
        // Get the token from storage. If no token in storage, go request a new one.
        console.log("requestWithJwt, url="+requestUrl);
        const token = this.getToken();
        console.log("current token: "+token);
        if (!token) {
            return new Promise(resolve => {
                resolve(this.requestJwt());
            });
        }

        // Add any provided HTTP body data to the body of the request
        const requestBody = {};
        if (bodyData) {
            Object.keys(bodyData).forEach((key) => requestBody[key] = bodyData[key]);
        }
        // Include the JWT in the Authorization header of the request
        const requestHeaders = new Headers({
            'Authorization' : 'Bearer ' + token,
            'Content-Type' : 'application/json'
        });
        return this.http.request(requestUrl, new RequestOptions({
            method: requestMethod,
            headers : requestHeaders,
            body : requestBody
        })).toPromise();
    }

    private getToken(): string {
        // Check if token already exists in storage
        const token = this.getFromStorage(AuthService.ACCESS_TOKEN);
        if (!token) {
            return undefined;
        }
        return token;
    }

    private requestJwt(): Promise<any> {
        // Record what service was requested so that we can re-navigate back to that service when we return
        this.storeRequestedService();
        console.log("Requesting token...");
        window.alert("Should not get here currently!");
        // Hard coded test
        return Promise.resolve("invalid");
/* TODO: enable this flow
        // Set all parameters required or recommended by OAuth implicit flow
        const redirectUri = this.getRedirectUri();
        const state = this.generateState(this.STATE_LENGTH);
        const params = {
            'response_type' : 'token',
            'client_id' : 'rp',
            'redirect_uri' : redirectUri,
            'state' : state,
            'scope' : 'openid email'
        };
        let queryString = '?';
        for (const param of Object.keys(params)) {
            queryString += encodeURIComponent(param) + '=';
            queryString += encodeURIComponent(params[param]) + '&';
        }

        const port = 31005;
        const authzUrl = location.protocol + '//' + location.hostname + ':' + port + '/oidc/endpoint/OP/authorize';
        const requestUrl = authzUrl + queryString;

        return new Promise(resolve => {
            window.location.replace(requestUrl);
        });
*/
    }

    private rerouteToRequestedService(): void {
        const lastRequestedService = this.getCookieValue(this.REQUESTED_SERVICE_COOKIE);
        this.deleteCookie(this.REQUESTED_SERVICE_COOKIE);
        if (lastRequestedService) {
            this.router.navigate([lastRequestedService]);
        }
    }

    private storeRequestedService(): void {
        let service = location.pathname;
        if (service.indexOf('?') > 0) {
            service = service.substring(0, service.indexOf('?'));
        }
        if (service.indexOf('#') > 0) {
            service = service.substring(0, service.indexOf('#'));
        }
        this.setCookie(this.REQUESTED_SERVICE_COOKIE, service, this.REQUESTED_SERVICE_COOKIE_EXP_SEC);
    }

    private setTokenExpiration() {
        // Determine when token expires and record expiration date in storage
        let expiresIn = parseInt(this.extractFromFragment(this.EXPIRES_IN), 10);
        if (!expiresIn) {
            expiresIn = this.DEFAULT_EXP_SEC;
        }
        const now = Date.now();
        const expiresAt = now + (expiresIn * 1000);
        this.setInStorage(this.EXPIRES_AT, expiresAt.toString());
    }

    private extractFromFragment(param: string): string {
        let result;
        const params = this.getFragmentParameters();
        if (params) {
            result = params[param];
        }
        return result;
    }

    private getFragmentParameters() {
        const fragment = location.hash;
        if (!fragment || fragment === '' || fragment === '#') {
            return undefined;
        }
        return this.getParameters(fragment.substr(1));
    }

    private getParameters(queryOrFragmentString: string) {
        const params = {};
        const paramObjs = queryOrFragmentString.split('&');
        paramObjs.forEach(function (param) {
            const entry = paramObjs[param].split('=');
            const key = entry[0];
            const value = entry[1];
            params[key] = value;
        });
        return params;
    }

    private getRedirectUri(): string {
        // Redirect back to root context since Angular mixed with OAuth redirects don't work well with deep linking
        const redirectUri = location.protocol + '//' + location.hostname;
        return redirectUri;
    }

    private generateState(length: number): string {
        let text = '';
        const chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
        for (let i = 0; i < length; i++ ) {
            text = text + chars.charAt(Math.floor(Math.random() * chars.length));
        }
        return text;
    }

    private setInStorage(param: string, value: string): void {
        window.localStorage.setItem(param, value);
    }

    private getFromStorage(param: string): string {
        return localStorage.getItem(param);
    }

    private setCookie(name: string, value: string, expireInMinutes: number): void {
        const d = new Date();
        d.setTime(d.getTime() + (expireInMinutes * 60 * 1000));
        const expires = 'expires=' + d.toUTCString();
        document.cookie = name + '=' + value + ';' + expires + ';path=/';
    }

    private getCookieValue(cookieName: string): string {
        const cookies = document.cookie;
        if (!cookies || cookies === '') {
            return undefined;
        }
        const cookiesSplit = cookies.split(';');
        cookiesSplit.forEach(function (element) {
            const cookie = element;
            const split = cookie.split('=');
            const name = split[0].trim();
            if (name === cookieName) {
                return split[1].trim();
            }
        });
        return undefined;
    }

    private deleteCookie(name: string): void {
        document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    }
}
