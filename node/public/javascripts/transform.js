/*
 * A transform class consisting of an arrow with a base,
 *       and pointing in the positive y direction.
 *
 * e.g. {base: {x: 0, y: 0}, dir: 0}
 */

function Arrow() {
    this.base = {x: 0, y: 0};
    this.dir = Math.PI / 2;
}

Arrow.prototype.translate = function(dx, dy) {
    this.base.x += dx * Math.sin(this.dir) + dy * Math.cos(this.dir);
    this.base.y += dy * Math.sin(this.dir) - dx * Math.cos(this.dir);
}

Arrow.prototype.rotate = function(angle) {
    this.dir += angle;
}

Arrow.prototype.scale = function(sx, sy) {
    var head = {
        x: this.base.x + Math.cos(this.dir),
        y: this.base.y + Math.sin(this.dir)
    };
    this.base.x *= sx;
    this.base.y *= sy;
    head.x *= sx;
    head.y *= sy;
    this.dir = Math.atan((head.y - this.base.y) / (head.x - this.base.x));
    if (head.x < this.base.x) {
        this.dir += Math.PI;
    }
}

Arrow.prototype.setContext = function(ctx) {
    ctx.translate(this.base.x, this.base.y);
    ctx.rotate(this.dir - Math.PI / 2);
}
