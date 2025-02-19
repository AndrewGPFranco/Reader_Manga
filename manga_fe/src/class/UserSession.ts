export class UserSession {
    private _firstName: string;
    private _fullName: string;
    private _username: string;
    private _email: string;
    private _dateBirth: Date;

    constructor(firstName: string, fullName: string, username: string, email: string, dateBirth: Date) {
        this._firstName = firstName;
        this._username = username;       
        this._fullName = fullName;
        this._dateBirth = dateBirth;
        this._email = email;
    }

    get firstName(): string {
        return this._firstName;
    }

    set firstName(value: string) {
        this._firstName = value;
    }

    get fullName(): string {
        return this._fullName;
    }

    set fullName(value: string) {
        this._fullName = value;
    }

    get username(): string {
        return this._username;
    }

    set username(value: string) {
        this._username = value;
    }

    get email(): string {
        return this._email;
    }

    set email(value: string) {
        this._email = value;
    }

    get dateBirth(): Date {
        return this._dateBirth;
    }

    set dateBirth(value: Date) {
        this._dateBirth = value;
    }
}