window.Color = {
    Menu: '4a4a4a',
    RightText: '28b6a4',
    BorderLine: 'e9e8e8',
    TitleColor: '333333',
    Gray: '9b9b9b',
    Gray_BG: 'f9f9f9',
    White: 'ffffff',
    Success: '46bd35'
};
window.BankSearchItem = {
    class: 'UIView',
    frame: { l: '22', r: '22', h: '54' },
    subViews: [
        {
            class: 'HeroLabel',
            frame: { x: '0', r: '100', y: '20', h: '14' },
            name: 'ID_Bank_Name',
            textColor: window.Color.BorderLine,
            size: 14
        },
        {
            class: 'UIView',
            frame: { x: '0', w: '1x', b: '0', h: '1' },
            backgroundColor: window.Color.BorderLine
        },
        {
            class: 'HeroLabel',
            name: 'ID_Bank_Code',
            frame: { r: '0', y: '20', h: '14', w: '100' },
            textColor: window.Color.BorderLine,
            size: 14
        }
    ]
};
window.BankFieldInput = {
    class: 'UIView',
    frame: { w: '1x', h: '77' },
    subViews: [
        {
            class: 'HeroLabel',
            name: '_ID_Label',
            size: 16,
            textColor: window.Color.Menu,
            frame: { x: '21', h: '18', y: '21', w: '1x' }
        },
        {
            class: 'HeroImageView',
            name: '_ID_Image',
            frame: { x: '23', w: '20', h: '20', y: '46' }
        }, {
            class: 'HeroTextField',
            name: '_ID_Input',
            size: 14,
            frame: { l: '51', r: '22', y: '30', h: '45' }
        }
    ]
};
window.DashboardMenu = {
    class: 'UIView',
    frame: { w: '0.3333x', h: '105' },
    subViews: [
        {
            class: 'HeroButton',
            frame: { w: '1x', h: '1x' }
        },
        {
            class: 'HeroImageView',
            name: 'icon',
            frame: { w: '30', h: '30' },
            center: { x: '0.5x', y: '40' }
        }, {
            class: 'HeroLabel',
            name: 'title',
            textColor: Color.TitleColor,
            size: 14,
            frame: { w: '1x', h: '12', b: '12' },
            alignment: 'center',
            text: ''
        }
    ]
};
