function ensureSlash(path, needsSlash) {
    var hasSlash = (path[path.length - 1] === '/');

    if (hasSlash && !needsSlash) {
        return path.substr(path, path.length - 1);
    } else if (!hasSlash && needsSlash) {
        return path + '/';
    } else {
        return path;
    }
}

window.host = window.location.origin;
window.path =  (window.location.protocol + '//' +  window.location.host + window.location.pathname).replace(/\/[A-Za-z0-9_-]+.html/, ''); // 如果url路径有前缀请加上;
window.path =  ensureSlash(window.path, false);
window.backgroundColor = 'ffffff';
window.color = '333333';
window.tintColor = '778899';
