import { getSessionNamesMap } from '@app/core/session-utils';

describe('session-utils', () => {
    const paramMap = {
        keys: null,
        params: {},
        has: null,
        get: null,
        getAll(name: string): string[] {
            return this.params[name];
        }
    };

    beforeEach(() => {
        paramMap.params = {};
    });

    it('uses the default session', () => {
        const sessionsMap = getSessionNamesMap(paramMap);

        const expectedMap = new Map();
        expectedMap.set('default', 'default');

        expect(sessionsMap).toEqual(expectedMap);
    });

    it('handles null session paramaters', () => {
        paramMap.params['session'] = null;
        const sessionsMap = getSessionNamesMap(paramMap);

        const expectedMap = new Map();
        expectedMap.set('default', 'default');

        expect(sessionsMap).toEqual(expectedMap);
    });

    it('handles empty session paramaters', () => {
        paramMap.params['session'] = [''];
        const sessionsMap = getSessionNamesMap(paramMap);

        const expectedMap = new Map();
        expectedMap.set('', '');

        expect(sessionsMap).toEqual(expectedMap);
    });

    it('handles null sessionName paramaters', () => {
        paramMap.params['session'] = ['1234567890'];
        paramMap.params['sessionName'] = null;
        const sessionsMap = getSessionNamesMap(paramMap);

        const expectedMap = new Map();
        expectedMap.set('1234567890', '1234567890');

        expect(sessionsMap).toEqual(expectedMap);
    });

    it('handles empty sessionName paramaters', () => {
        paramMap.params['session'] = ['1234567890'];
        paramMap.params['sessionName'] = [''];
        const sessionsMap = getSessionNamesMap(paramMap);

        const expectedMap = new Map();
        expectedMap.set('1234567890', '');

        expect(sessionsMap).toEqual(expectedMap);
    });
});
